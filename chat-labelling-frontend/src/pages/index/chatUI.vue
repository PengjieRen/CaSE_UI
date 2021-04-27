<template>
  <Card style="background:#efefef7a;min-height:400px;overflow:auto" ref="chatPanel">
    <message v-for="(m,index) in history" :data="m" :key="'history-'+index" />
    <Divider size="small" style="color:#aaa" v-if="history.length>0">History</Divider>
    <div style="min-height:200px">
      <message v-for="(m,index) in message" :data="m" :key="'message-'+index" />
    </div>
  </Card>
</template>
<script>
import message from '../../components/message'
const messageType = {
  user: {position: 'right', user: 'Me', color: true},
  sys: {position: 'left', user: 'Partner', color: false},
  other: {position: 'left', color: 'green', user: 'Sys'}
}
export default {
  props: {
    loadHistory: {
      type: Function
    }
  },
  components: {
    message
  },
  data () {
    return {
      history: [],
      message: [],
      loading: false
    }
  },
  methods: {
    reset () {
      this.history = []
      this.message = []
    },
    addMessage (message, type) {
      if (messageType[type]) {
        this.message.push({...message, ...messageType[type]})
      } else {
        this.message.push({...message, ...messageType.other, user: type || 'UserUnknown'})
      }
      this.$nextTick(() => {
        let container = this.$refs.chatPanel.$el
        container.scrollTop = container.scrollHeight
      })
    },
    addHistoy (data) {
      data.forEach(element => {
        if (messageType[element.type]) {
          this.history.push({...element, ...messageType[element.type]})
        } else {
          this.history.push({...element, ...messageType.other, user: element.type ? element.type : 'UserUnknown'})
        }
      })
      this.$nextTick(() => {
        let container = this.$refs.chatPanel.$el
        container.scrollTop = container.scrollHeight
      })
    }
  }

}
</script>
