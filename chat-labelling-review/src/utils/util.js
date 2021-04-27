export default {
  getUUid () {
    let s = []
    let hexDigits = '0123456789abcdef'
    for (let i = 0; i < 36; i++) {
      s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1)
    }
    s[14] = '4'
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1)
    s[8] = s[13] = s[18] = s[23] = '-'

    let uuid = s.join('')
    return uuid.replace(/-/g, '')
  },
  initAxios (axios, ViewUI, Vue) {
    axios.defaults.timeout = 15000
    axios.defaults.withCredentials = true
    axios.interceptors.request.use((config) => {
      // ViewUI.LoadingBar.start()
      return config
    }, function (error) {
      // ViewUI.LoadingBar.error()
      return Promise.reject(error)
    })
    axios.interceptors.response.use((config) => {
      // ViewUI.LoadingBar.finish()
      return config
    }, function (error) {
      // ViewUI.LoadingBar.error()
      return Promise.reject(error)
    })

    Vue.prototype.$http = axios
  }
}
