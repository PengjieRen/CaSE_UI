import Vue from 'vue'
import index from './login.vue'
import ViewUI from 'view-design'
import axios from 'axios'
import './../theme.less'
import utils from '../../utils/util'

utils.initAxios(axios, ViewUI, Vue)

Vue.use(ViewUI)
// eslint-disable-next-line no-new
new Vue({
  el: '#app',
  render: h => h(index)
})
