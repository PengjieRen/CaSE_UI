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
      let defaultParams = this.parseUrl(window.location.href)
      // ViewUI.LoadingBar.start()
      if (config.headers && config.headers['Content-Type'] === 'application/x-www-form-urlencoded') return config
      config.params = {
        ...(config.params ? config.params : {}),
        _t: Date.parse(new Date()),
        ...defaultParams
      }
      return config
    }, function (error) {
      ViewUI.Notice.error(
        {
          title: 'Error',
          desc: 'Something wrong when requesting data!'
        })
      // ViewUI.LoadingBar.error()
      return Promise.reject(error)
    })
    axios.interceptors.response.use((config) => {
      // ViewUI.LoadingBar.finish()
      return config
    }, function (error) {
      console.log(error.response)
      ViewUI.Notice.error(
        {
          title: 'Error',
          desc: 'Something wrong when responding data!' + (error.response && error.response.data) ? error.response.data.message : ''
        })
      return Promise.reject(error)
    })

    Vue.prototype.$http = axios
  },
  parseUrl (url) {
    var query = url.split('?')[1]
    if (!query) return {}
    var obj = {}
    var queryArr = query.split('&')
    queryArr.forEach(function (item) {
      var value = item.split('=')[1]
      var key = item.split('=')[0]
      obj[key] = value
    })
    return obj
  }
}
