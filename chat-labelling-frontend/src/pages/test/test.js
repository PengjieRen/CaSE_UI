import Vue from 'vue'
import index from './test.vue'
import ViewUI from 'view-design'
import locale from 'view-design/dist/locale/en-US'
import './../theme.less'
import axios from 'axios'
import utils from '../../utils/util'
import VueFriendlyIframe from 'vue-friendly-iframe'
import VueClipboard from 'vue-clipboard2'
import VueJsonp from 'vue-jsonp'

Vue.use(VueFriendlyIframe)

utils.initAxios(axios, ViewUI, Vue)
Vue.use(ViewUI, { locale })
Vue.use(VueFriendlyIframe)
Vue.use(VueClipboard)
Vue.use(VueJsonp)

Vue.prototype.$log = (logData) => {
  // axios.post('/api/addLog', {type: logData.type, content: JSON.stringify(logData)}).then(response => {
  // })
  console.log(logData)
}
// eslint-disable-next-line no-new
new Vue({
  el: '#app',
  render: h => h(index)
})
