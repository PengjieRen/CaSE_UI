<style scoped>
.wrap {
  position: relative;
  overflow: hidden;
  margin-right: 4px;
  margin-left: 20px;
  display: inline-block;
  padding: 4px 10px;
  line-height: 18px;
  text-align: center;
  vertical-align: middle;
  cursor: pointer;
  background: #2d8cf0;
  border: 1px dotted #2d8cf0;
  border-radius: 4px;
  -webkit-border-radius: 4px;
  -moz-border-radius: 4px;
  font-size: 14px;
  font-weight: 100;
}
.wrap:hover {
  background-color: #57a3f3;
  border-color: #57a3f3;
}
.wrap span {
  color: #fff;
}
.wrap .file {
  position: absolute;
  top: 0;
  right: 0;
  margin: 0;
  border: solid transparent;
  opacity: 0;
  filter: alpha(opacity=0);
  cursor: pointer;
}
</style>
<template>
  <div>
    <Card dis-hover :bordered="false" :padding="8">
      <div slot="title" style="text-align:center">
        <strong>
          当前文件：
          <tag color="blue" type="border" v-if="this.fileName">{{
              fileName
            }}</tag>
          <span v-else>无</span>
        </strong>
        <div class="wrap">
          <span> <Icon type="md-open" />打开已纠错文件 </span>
          <input
            id="labelledUpload"
            class="file"
            type="file"
            @change="readLabelled"
            ref="labelledUpload"
          />
        </div>
        <Button type="text" @click="generateFile">
          <Icon type="md-download" />下载已纠错文件
        </Button>
      </div>
      <Spin size="large" v-if="loading" fix>
        <Icon type="ios-loading" size="50" class="spin-icon-load"></Icon>
        <div>Loading...</div>
      </Spin>
      <div>
        <div v-if="dialogIntent" style="border-radius:10px;padding: 6px 8px;border: 1px solid #2b85e4;">
          <span style="color: red;">conversation_id:</span> {{dialogId}}<br/>
          <span style="color: red;">from:</span>{{dialogFrom}}<br/>
          <span style="color: red;">intent:</span>{{dialogIntent}}</div>
        <Row :gutter="8">
          <Col :span="10">
            <Card
              id="chatPanel"
              style="background:#efefef7a;min-height:400px;overflow:auto"
              :style="{ height: height + 'px'}"
              ref="chatPanel"
            >
              <div
                v-for="(m, index) in messages"
                :key="'message-' + index"
                @click="messageSelected(index)"
                style="border-radius:10px;padding:3px"
                :style="{
                  border:
                    currentIndex === index ? '1px dashed #1890ff ' : 'none'
                }"
              >
                <message :data="m" :id="'message_'+index"/>
              </div>
            </Card>
          </Col>
          <Col :span="14" v-if="messages.length > 0">
            <div
              style="padding:10px"
              :style="{ height: height + 'px' }"
            >
              <Page v-if="dialogs.length" :total="dialogs.length" @on-change="dialogChanged" :page-size="1" show-elevator style="margin: 10px"/>
              <ButtonGroup size="small" style="margin-left:10px;margin-bottom:10px">
                <Button
                  type="primary"
                  @click="messageSelected(currentIndex-1)"
                  :disabled="currentIndex === 0"
                >
                  <Icon type="ios-arrow-back" />上一条
                </Button>
                <Button
                  type="primary"
                  @click="messageSelected(currentIndex + 1)"
                  :disabled="currentIndex === messages.length - 1"
                >
                  下一条
                  <Icon type="ios-arrow-forward" />
                </Button>
              </ButtonGroup>
              <labelling
                v-if="messages[currentIndex].role ==='user'"
                ref="labelling"
                :message="messages[currentIndex]"
                :role="messages[currentIndex].role"
                :sendDisabled="disabled"
                :actions="allActions"
                :conversationId="dialogId"
              />
              <labellingSystem
                v-if="messages[currentIndex].role ==='system'"
                ref="labellingSystem"
                :message="messages[currentIndex]"
                :role="messages[currentIndex].role"
                :sendDisabled="disabled"
                :actions="allActions"
                :conversationId="dialogId"
              />
            </div>
          </Col>
        </Row>
      </div>
    </Card>
  </div>
</template>
<script>
import message from '../../components/message'
import labelling from './labelling'
import labellingSystem from './labellingSystem'

const reader = new FileReader()
export default {
  components: {
    message,
    labelling,
    labellingSystem
  },
  data () {
    return {
      height: window.screen.availHeight - 150,
      fileName: '',
      dialogs: [], // 所有对话数据
      messages: [], // 一次对话数据（包括系统和用户间的多轮对话）
      dialogId: '',
      dialogIntent: '',
      dialogFrom: '',
      userConfig: {...window.chatterConfig},
      currentDialogIndex: 0, // 第几段对话
      currentIndex: 0, // 一次对话中的第几次回答
      loading: false,
      allActions: {...window.allActions},
      disabled: false
    }
  },
  methods: {
    messageSelected (index) {
      console.log('this.messages=', this.messages[index])
      let role = ''
      if (index !== 0) {
        role = this.messages[index - 1].role
      } else {
        role = 'user'
      }
      console.log('role=', role)
      if (role === 'user') {
        console.log(this.$refs.labelling && this.messages.splice(this.currentIndex, 1, this.$refs.labelling.getUpdated()))
      } else {
        console.log(this.$refs.labellingSystem && this.messages.splice(this.currentIndex, 1, this.$refs.labellingSystem.getUpdated()))
      }
      this.currentIndex = index
      console.log('this.currentIndex=', this.currentIndex)
    },
    readLabelled (event) { // 打开已标注的文件
      this.loading = true
      let file = event.target.files[0]
      this.messages = []
      this.dialogs = []
      this.currentIndex = 0
      reader.addEventListener('loadend', () => {
        let fileData = JSON.parse(reader.result)
        this.dialogs = fileData // 获取文件数据,所有的对话数据数组[{},{},{}]
        this.dialogChanged(1) // 初始展示文件中第一组数据
        this.fileName = file.name
        this.loading = false
      })
      reader.readAsText(file) // readAsText 方法可以将 Blob 或者 File 对象转根据特殊的编码格式转化为内容(字符串形式)
    },
    dialogChanged (page) { // 根据用户操作选择的第几条，展示相应的对话内容
      // 以下两处负责更新修改结果，在切换页面的时候更新
      if (this.$refs.labelling) {
        this.messages[this.currentIndex] = {...this.$refs.labelling.getUpdated()}
        // this.$set(this.data,”key”,value’)
        this.$set(this.dialogs[this.currentDialogIndex], 'conversations', this.messages)
        this.$set(this.dialogs[this.currentDialogIndex], 'conversation_id', this.dialogId)
        this.$set(this.dialogs[this.currentDialogIndex], 'intent', this.dialogIntent)
        this.$set(this.dialogs[this.currentDialogIndex], 'from', this.dialogFrom)
      }
      if (this.$refs.labellingSystem) {
        this.messages[this.currentIndex] = {...this.$refs.labellingSystem.getUpdated()}
        this.$set(this.dialogs[this.currentDialogIndex], 'conversations', this.messages)
        this.$set(this.dialogs[this.currentDialogIndex], 'conversation_id', this.dialogId)
        this.$set(this.dialogs[this.currentDialogIndex], 'intent', this.dialogIntent)
        this.$set(this.dialogs[this.currentDialogIndex], 'from', this.dialogFrom)
      }
      this.dialogId = this.dialogs[page - 1].conversation_id
      this.dialogIntent = this.dialogs[page - 1].intent
      this.dialogFrom = this.dialogs[page - 1].from
      this.currentDialogIndex = page - 1 // 初始index从0开始，而右侧分页是从1开始，所以需减一
      this.messages = [] // 每轮对话语句中加上position，color，topText三个前端展示需要的数据，以形成该轮对话的messages
      this.dialogs[page - 1].conversations && this.dialogs[page - 1].conversations.forEach(message => {
        let role = message.role
        this.messages.push({
          ...message,
          position: role === 'system' ? 'left' : 'right',
          color: role !== 'system',
          topText: '已标注'
        })
      })
      this.currentIndex = 0
    },
    generateFile () { // 把已标注数据转化为文件供下载
      if (this.$refs.labelling) {
        this.messages[this.currentIndex] = {...this.$refs.labelling.getUpdated()}
        this.$set(this.dialogs[this.currentDialogIndex], 'conversations', this.messages)
        this.$set(this.dialogs[this.currentDialogIndex], 'conversation_id', this.dialogId)
        this.$set(this.dialogs[this.currentDialogIndex], 'intent', this.dialogIntent)
        this.$set(this.dialogs[this.currentDialogIndex], 'from', this.dialogFrom)
      }
      if (this.$refs.labellingSystem) {
        this.messages[this.currentIndex] = {...this.$refs.labellingSystem.getUpdated()}
        this.$set(this.dialogs[this.currentDialogIndex], 'conversations', this.messages)
        this.$set(this.dialogs[this.currentDialogIndex], 'conversation_id', this.dialogId)
        this.$set(this.dialogs[this.currentDialogIndex], 'intent', this.dialogIntent)
        this.$set(this.dialogs[this.currentDialogIndex], 'from', this.dialogFrom)
      }
      // this.actions[this.currentIndex] = [...this.currentActions]
      console.log(this.dialogs[this.currentDialogIndex])
      let aTag = document.createElement('a')
      let json = []
      // let finish = true
      let i = 0
      for (i = 0; i < this.dialogs.length; i++) {
        let id = this.dialogs[i].conversation_id
        let intent = this.dialogs[i].intent
        let from = this.dialogs[i].from
        let msgs = this.dialogs[i].conversations
        msgs = msgs.map(m => {
          if (m.role === 'user') {
            return {
              msg_id: m.msg_id,
              turn_id: m.turn_id,
              role: m.role,
              intent: m.intent,
              response: m.response
            }
          } else {
            return {
              msg_id: m.msg_id,
              turn_id: m.turn_id,
              role: m.role,
              state: m.state,
              query_candidate: m.query_candidate,
              result_candidate: m.result_candidate,
              action: m.action,
              selected_result: m.selected_result,
              selected_query: m.selected_query,
              response: m.response
            }
          }
        })
        json[i] = {
          conversation_id: id,
          conversations: msgs,
          intent: intent,
          from: from
        }
      }
      var blob = new Blob([JSON.stringify(json, null, 2)])
      aTag.download =
        this.fileName
      aTag.href = URL.createObjectURL(blob)
      aTag.click()
      URL.revokeObjectURL(blob)
    }
  }
}
</script>
