<template>
  <div style="padding:10px 30px">
    <Form
      :model="formItem"
      :label-width="100"
      @submit.native.prevent
      :rules="rules"
      ref="form"
    >
      <FormItem label="Intent" prop="action">
        <customCascader
          :data="actions.user"
          v-model="formItem.intent"
          placeholder="Select"
          @on-change="actionChanged"
          @showTooltip="showTooltip"
        ></customCascader>
      </FormItem>
      <FormItem label="Response" prop="response">
        <Input v-model="formItem.response" @on-blur="responseChanged" type="textarea" :rows="4" />
      </FormItem>
    </Form>
  </div>
</template>
<script>
import customCascader from '../../components/customCascader'
import helpTooltip from '../../components/helpTooltip'
import draggableList from '../../components/draggableList'
import createSelect from '../../components/createSelect'

const calAllow = (arr, actionNames, actionNameIndex) => {
  if (!actionNames[actionNameIndex]) return false
  let a = arr.filter(value => {
    return actionNames[actionNameIndex] === value.name
  })
  if (!a[0]) return false
  if (a[0].children) {
    return calAllow(a[0].children, actionNames, actionNameIndex + 1)
  } else { return a[0] && a[0].allowEmptySearch === true }
}

export default {
  components: {
    helpTooltip,
    draggableList,
    customCascader,
    createSelect
  },
  props: {
    conversationId: {
    },
    height: {
      type: String
    },
    role: {
      type: String
    },
    finish: {
      type: Function // 用户角色自主结束会话的方法
    },
    actions: {}, // 全部action
    // currentState: {}, // 上一轮过后，state是什么（数据库里的state，系统用户没进行修改）
    searchData: {}, // 调用api后搜索结果，每次state改变都会重新搜索
    message: {}
  },
  watch: {
    message (newValue, oldValue) {
      console.log('newValue=', newValue)
      this.formItem.intent = [...newValue.intent] // 新加了intent
      this.formItem.response = newValue.response
    }
  },
  data () {
    return {
      formItem: {
        intent: [...this.message.intent], // 新加了intent
        response: this.message.response
      },
      rules: {
        response: [{
          required: true,
          type: 'string',
          message: 'Please fill in response!',
          trigger: 'blur'
        }]
      },
      drawer: false,
      sendLog: true,
      searchPanelLoading: false,
      searchResults: {},
      // actionBackup: [...this.message.action], // 上一次sys action改变后的值
      submitting: false // 为了避免在发送数据时，用户进行别的操作
    }
  },
  methods: {
    reset () {
      this.sendLog = false // 重置界面期间数据的改变不发log
      this.formItem = {
        intent: [], // 新加了intent
        response: ''
      }
      this.drawer = false
      this.searchPanelLoading = false
      this.searchResults = {}
      this.actionBackup = []
      this.$nextTick(() => {
        this.sendLog = true
      })
    },
    cascaderVisableChange (show) {
      if (show) { this.drawer = true }
    },
    actionChanged (actions) {
      // 如果是系统角色，改了相应action，对应要清空之前选择的search result
      // if (this.role === 'sys' && this.formItem.selectedSearch.length > 0) {
      //   // actionBackup中存的是上一次sys action改变后的值
      // // 比较与当前action的第一级是否相同，如果相同，则selectedSearch不做修改，否则会清空已选result
      //   if (this.actionBackup[0] !== actions[0]) {
      //     this.formItem.selectedSearch = []
      //   }
      //   this.actionBackup = [...actions]
      // }
      if (this.sendLog && !this.sendDisabled && this.conversationId) {
        this.$log({
          conversationId: this.conversationId,
          type: 'actionChanged',
          data: {actions}
        })
      }
    },
    responseChanged () {
      if (this.sendLog && !this.sendDisabled && this.conversationId && this.formItem.response) {
        this.$log({
          conversationId: this.conversationId,
          type: 'responseChanged',
          data: {response: this.formItem.response}
        })
      }
    },
    showTooltip (item) {
      if (!this.sendDisabled && this.sendLog && this.conversationId) {
        this.$log({
          conversationId: this.conversationId,
          type: 'showTooltip',
          data: {item: {...item}}
        })
      }
    },
    canCreateState (value) {
      if (value.split(' ').length >= 5) {
        return {
          canCreate: false,
          message: 'Too many words!'
        }
      }
      return {
        canCreate: true
      }
    },
    getUpdated () {
      let temp = { ...this.message }
      temp.intent = this.formItem.intent
      temp.response = this.formItem.response
      return temp
    }
  }
}
</script>
