import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import iView from 'iview'
import axios from 'axios'
import VueI18n from 'vue-i18n'
import numeral from 'numeral'

import 'iview/dist/styles/iview.css'
import en_US from 'iview/dist/locale/en-US'
import zh_CN from 'iview/dist/locale/zh-CN'
import zh_TW from 'iview/dist/locale/zh-TW'
import http from './common/http'
import { getInitConfig } from './common/native'

Vue.use(VueI18n)
Vue.use(iView)

Vue.config.productionTip = false

// Setting i18n
const i18n = new VueI18n({
  locale: 'zh-CN',
  messages: {
    'en-US': Object.assign(require('./i18n/en-US').default, en_US),
    'zh-CN': Object.assign(require('./i18n/zh-CN').default, zh_CN),
    'zh-TW': Object.assign(require('./i18n/zh-TW').default, zh_TW)
  }
})

Vue.prototype.$noSpinHttp = axios.create()
Vue.prototype.$http = http.build()
Vue.prototype.$http.interceptors.response.use(
  response => {
    return response
  },
  error => {
    if (!error.response) {
      Vue.prototype.$Message.error(i18n.t('alert.refused'))
    } else if (error.response.status == 400) {
      let i18nKey =
        'alert["' +
        new URL(error.config.url).pathname +
        '"]' +
        '.' +
        error.config.method +
        '.' +
        error.response.data.code
      Vue.prototype.$Message.error(i18n.t(i18nKey))
    } else if (error.response.status == 404) {
      Vue.prototype.$Message.error(i18n.t('alert.notFound'))
    } else if (error.response.status == 504) {
      Vue.prototype.$Message.error(i18n.t('alert.timeout'))
    } else {
      Vue.prototype.$Message.error(i18n.t('alert.error'))
    }
    return Promise.reject(error)
  }
)

Vue.prototype.$numeral = numeral
Date.prototype.format = function(fmt) {
  var o = {
    'M+': this.getMonth() + 1, // Month
    'd+': this.getDate(), // Day
    'h+': this.getHours(), // Hour
    'm+': this.getMinutes(), // Minute
    's+': this.getSeconds(), // Second
    'q+': Math.floor((this.getMonth() + 3) / 3), // Quarter
    S: this.getMilliseconds() // Millisecond
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(RegExp.$1, (this.getFullYear() + '').substr(4 - RegExp.$1.length))
  }
  for (var k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(RegExp.$1, RegExp.$1.length === 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length))
    }
  }
  return fmt
}

Promise.prototype.finally = function(callback) {
  let P = this.constructor
  return this.then(
    value => P.resolve(callback()).then(() => value),
    reason =>
      P.resolve(callback()).then(() => {
        throw reason
      })
  )
}

// Change the page according to the routing changes title
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `Proxyee Down-${to.meta.title}`
  }
  next()
})

// Get client configuration information
getInitConfig()
  .then(result => {
    Vue.prototype.$config = result
    // Set default language
    i18n.locale = result.locale
  })
  .finally(() => {
    new Vue({
      router,
      store,
      i18n,
      render: h => h(App)
    }).$mount('#app')
  })
