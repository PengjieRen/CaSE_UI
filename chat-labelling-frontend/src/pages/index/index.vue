<template>
  <div>
    <Modal v-model="showInstructions" draggable scrollable title="Instructions">
      <div v-html="instructions"></div>
      <div slot="footer">
        <Button type="primary" @click="showInstructions=false">OK</Button>
      </div>
    </Modal>
    <profile v-show="needProfile" @completed="profileCompleted"/>
    <Card :bordered="false" dis-hover v-show="!finished&&!needProfile">
      <p slot="title" style="text-align:center;overflow:hidden">
        <strong slot="title">
          <Icon type="ios-chatbubbles-outline" :size="20" />
          Current user is {{username}}, chatting with user {{partner}}
        </strong>
      </p>
      <div slot="extra">
        <Button type="text" @click="showInstructions=true">
          <Icon type="ios-information-circle-outline" />Instructions
        </Button>
      </div>
      <Spin size="large" v-if="loading" fix>
        <Icon type="ios-loading" size="50" class="spin-icon-load"></Icon>
        <div>{{message}}, please don't leave...</div>
      </Spin>
      <div>
        <Row :gutter="8">
          <Col :span="10">
            <chatUI ref="chatUI" :style="{height:(height+35)+'px',overflow:'auto'}" />
          </Col>
          <Col :span="14">
            <Alert v-if="role==='cus'">
              <strong>Background:</strong>
              <p style="font-size:80%">{{background}}</p>
            </Alert>
            <Alert type="warning" v-if="!finished">
              <strong>Note:</strong>
              <ul style="padding:0 10px;line-height:150%;font-size:80%">
                <li>
                  Refreshing or reloading your page may
                  <strong>terminate</strong> the conversation!
                </li>
                <li v-if="disabled">
                  Please wait for your partner's message.
                  <Button
                    style="margin-left:10px"
                    type="primary"
                    size="small"
                    @click="hurryup"
                    :disabled="hurryupLeftSeconds>0"
                  >Hurry up!{{hurryupLeftSeconds>0? ('('+hurryupLeftSeconds+'s)'):''}}</Button>
                </li>
              </ul>
            </Alert>
            <labelling
              :height="(height-20-(role=='cus'? 60:0))+'px'"
              ref="labelling"
              :role="role"
              :currentState="currentState"
              :searchData="searchData"
              :sendData="sendData"
              :sendDisabled="disabled"
              :actions="actions"
              :conversationId="conversationId"
              :finish="finish"
            />
          </Col>
        </Row>
      </div>
    </Card>
    <rating v-show="finished&&!needProfile" :role="role" :conversationId="conversationId"  @completed="ratingCompleted"/>
  </div>
</template>
<script>
import chatUI from './chatUI'
import profile from './profile'
import labelling from './labelling'
import rating from './rating'
import util from '../../utils/util'
const maxHurryupLeftSeconds = 60
export default {
  components: {
    chatUI,
    labelling,
    rating,
    profile
  },
  mounted () {
    this.init()
    window.addEventListener('beforeunload', e => this.beforeunloadFn(e))
  },
  destroyed () {
    window.removeEventListener('beforeunload', e => this.beforeunloadFn(e))
    this.$socket.close()
  },
  data () {
    return {
      height: window.screen.availHeight - 230,
      loading: false,
      message: '',
      username: '',
      partner: '',
      conversationId: '',
      disabled: false,
      role: '',
      hurryupLeftSeconds: 0,
      actions: [],
      currentState: [],
      finished: false,
      needProfile: false,
      background: '',
      instructions: '',
      showInstructions: false
    }
  },
  methods: {
    beforeunloadFn (e) {
      if (this.finished) return
      // 这个事件只有在鼠标真正和浏览器有了交互，再刷新或者关闭时才会触发
      e = e || window.event
      if (e) {
        e.returnValue = '关闭提示'
      }
      return '关闭提示'
    },
    async init () {
      this.loadUser()
      this.loading = true
      this.message = 'Connecting to server'
      this.initWebSocket()
      // this.message = 'Checking profile'
      await this.checkProfile()
      // this.message = 'Loading instructions'
      await this.loadInstructions()
      // this.message = 'Loading actions'
      await this.loadAction()
      // this.message = 'Waiting for commands from server'
    },
    profileCompleted () {
      this.needProfile = false
    },
    ratingCompleted () {
      window.location = '/'
    },
    loadInstructions () {
      return this.$http.get('/api/instructions').then((response) => {
        this.instructions = response.data
      })
    },
    checkProfile () {
      return this.$http.get('/api/checkProfile').then((response) => {
        this.needProfile = response.data
      })
    },
    initWebSocket () {
      let loc = window.location
      this.$connect('ws://' + loc.hostname + ':' + loc.port + '/websocket/' + this.username)
      this.$socket.onopen = this.websocketonopen
      this.$socket.onerror = this.websocketonerror
      this.$socket.onmessage = this.websocketonmessage
      this.$socket.onclose = this.websocketclose
    },
    finish (data, callback) {
      this.loading = true
      this.message = 'Finishing conversation'
      this.websocketsend({type: 'FINISH'})
      callback()
    },
    reset () {
      this.loading = true
      this.message = ''
      this.partner = ''
      this.disabled = false
      this.role = ''
      this.finished = false
      this.$refs.chatUI.reset()
      this.$refs.labelling.reset()
    },
    websocketonopen (e) {
      // console.log('WebSocket连接成功', e)
    },

    websocketonerror (e) {
      // 错误
      this.$Notice.error(
        {
          title: 'Error',
          desc: 'Something wrong on the server!'
        })
    },
    websocketonmessage (e) {
      const dataList = JSON.parse(e.data)
      dataList.map(async data => {
        // 数据接收，同步的执行方式，load数据完之后，才能往下执行
        if (data.messageCommand === 'START' && !this.finished) {
          this.$Notice.info(
            {
              title: 'Notice',
              desc: 'Your partner is online!'
            }
          )
          this.reset()
          let tempData = data.data
          this.partner = tempData.partner
          this.role = tempData.role
          this.conversationId = tempData.conversationId
          this.loading = true
          this.message = 'Loading history and Background'
          await this.loadHistoryAndBackground()
          this.loading = false
        }
        if (data.messageCommand === 'WAIT4PARTNER' && !this.finished) {
          this.message = 'Finding for your partner'
          this.partner = ''
          this.role = ''
          this.loading = true
        }
        if (data.messageCommand === 'SENDMESSAGE' && !this.finished) {
          if (data.content) {
            this.$refs.chatUI.addMessage({message: data.content, time: new Date()}, 'sys')
          }
          if (this.role === 'sys') {
            this.loading = true
            this.disabled = true
            this.message = 'Loading states'
            await this.loadCurrentState()
          }
          this.loading = false
          this.disabled = false
        }
        if (data.messageCommand === 'WAIT4MESSAGE' && !this.finished) {
          if (data.content) {
            this.$refs.chatUI.addMessage({message: data.content, time: new Date()}, 'sys')
          }
          this.loading = false
          this.disabled = true
        }
        if (data.messageCommand === 'ERROR' && !this.finished) {
          if (data.conversationId >= 0) {
            this.$Notice.error(
              {
                title: 'Error',
                desc: data.content
              }
            )
          } else {
            this.loading = true
            this.$Modal.warning({
              title: 'Notice',
              content: data.content
            })
          }
        }
        if (data.messageCommand === 'STOP' && !this.finished) {
          this.$Notice.error(
            {
              title: 'Error',
              desc: 'Your partner is offline!'
            }
          )
          this.message = 'Waiting for your partner reconnect to server'
          this.loading = true
        }
        if (data.messageCommand === 'HURRYUP' && !this.finished) {
          this.$Notice.warning(
            {
              title: 'Notice',
              desc: 'Hurry up please!'
            }
          )
        }
        if (data.messageCommand === 'FINISH') {
          this.finished = true
          this.$Notice.warning(
            {
              title: 'Notice',
              desc: 'The conversationclient is finished, please rate it before leave.'
            }
          )
          this.loading = false
          this.disabled = true
        }
      })
    },
    websocketsend (data) {
      this.$socket.send(JSON.stringify({
        from: this.username,
        to: this.partner,
        type: this.role === 'sys' ? 'SYS2CUS' : 'CUS2SYS',
        ...data
      }))
    },
    hurryup () {
      this.$socket.send(JSON.stringify({
        content: 'hurry up!',
        from: this.username,
        to: this.partner,
        type: 'HURRYUP'
      }))
      this.hurryupLeftSeconds = maxHurryupLeftSeconds
      let interval = setInterval(() => {
        this.hurryupLeftSeconds--
        if (this.hurryupLeftSeconds === 0) {
          clearInterval(interval)
        }
      }, 1000)
    },
    websocketclose (e) {
      // 关闭链接时触发
      var code = e.code//  状态码表 https://developer.mozilla.org/zh-CN/docs/Web/API/CloseEvent
      var reason = e.reason
      var wasClean = e.wasClean
      console.log(code, reason, wasClean)
    },
    loadUser () {
      this.username = util.parseUrl(location.href)._user
    },
    loadHistoryAndBackground () {
      const params = {
        sysName: this.role === 'sys' ? this.username : this.partner,
        cusName: this.role === 'cus' ? this.username : this.partner,
        conversationId: this.conversationId
      }
      return this.$http.get('/api/loadHistoryAndBackground', {params}).then((response) => {
        // todo 处理history
        if (response.data.history) {
          let history = []
          response.data.history.map(value => {
            history.unshift({
              message: value.content,
              time: value.updateTime,
              type: (value.type === 'CUS2SYS' && this.role === 'cus') || (value.type === 'SYS2CUS' && this.role === 'sys') ? 'user' : 'sys'
            })
          })
          this.$refs.chatUI.addHistoy(history)
        }
        if (response.data.background) {
          this.$refs.chatUI.addMessage({
            message: '<strong>Role: </strong>' + (this.role === 'sys' ? 'System, ' : 'Normal user. ') + '<br/>' +
              (this.role === 'cus' ? '<strong>Background: </strong>' + response.data.background + '<br/>' : '') +
              '<strong>Now chat start!</strong>',
            time: new Date()
          }, 'other')
          if (this.role === 'cus') {
            this.background = response.data.background
          } else {
            this.background = 'Please try to help your partner get his/her target information.'
          }
        }
      })
    },
    loadCurrentState () {
      return this.$http.get('/api/loadCurrentState').then((response) => {
        this.currentState = response.data
      })
    },
    searchData (states, callback) {
      if (states.length === 0) {
        return
      }
      this.$jsonp('http://localhost:9191?query=' + encodeURIComponent(states.join(' ')), {
        callbackQuery: 'callback',
        callbackName: 'searchDataCallback'
      }).then((json) => {
        this.$http.post('/api/saveSearchResults', {
          query: states.join(' '),
          conversationId: this.conversationId,
          data: json
        })
        callback(json)
      }).catch((error) => {
        console.log(error)
        this.$Notice.error(
          {
            title: 'Error',
            desc: 'Please check your proxy application!'
          })
        let emptyData = {
          Answer: [],
          Suggest: []
        }
        callback(emptyData)
      })
    },
    loadAction () {
      return this.$http.get('/api/loadActions').then((response) => {
        this.actions = response.data
      })
    },
    sendData (data, callback) {
      this.loading = true
      this.message = 'Sending message'
      this.websocketsend(data)
      this.$refs.chatUI.addMessage({message: data.response, time: new Date()}, 'user')
      callback()
    }
  }
}
</script>
