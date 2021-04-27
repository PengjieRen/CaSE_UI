import Vue from 'vue'
import index from './index.vue'
import ViewUI from 'view-design'
import locale from 'view-design/dist/locale/en-US'

import axios from 'axios'
import './../theme.less'
import utils from '../../utils/util'
import VueNativeSock from 'vue-native-websocket'
import VueClipboard from 'vue-clipboard2'
import VueJsonp from 'vue-jsonp'

utils.initAxios(axios, ViewUI, Vue)
Vue.use(VueClipboard)
Vue.use(VueJsonp)

Vue.use(VueNativeSock, 'ws://localhost:8080/websocket', {
  connectManually: true,
  reconnection: true, // (Boolean)是否自动重连，默认false
  reconnectionAttempts: 5, // 重连次数
  reconnectionDelay: 3000 // 再次重连等待时常(1000)
})
Vue.use(ViewUI, { locale })
Vue.prototype.$log = (logData) => {
  axios.post('/api/addLog', {
    type: logData.type,
    content: JSON.stringify(logData),
    conversationId: logData.conversationId
  }).then(response => {
  })
}
// eslint-disable-next-line no-new
new Vue({
  el: '#app',
  render: h => h(index)
})
