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
import { getLocale } from './common/native'

Vue.use(VueI18n)
Vue.use(iView)

Vue.config.productionTip = false

// 设置i18n
// setting i18n
const i18n = new VueI18n({
  locale: 'zh-CN',
  messages: {
    'en-US': Object.assign(require('./i18n/en-US').default, en_US),
    'zh-CN': Object.assign(require('./i18n/zh-CN').default, zh_CN)
  }
})

// 设置语言类型
// setting locale
getLocale().then(locale => (i18n.locale = locale))

Vue.prototype.$http = axios.create()
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

// 路由发生变化修改页面title
// change the page according to the routing changes title
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `Proxyee Down-${to.meta.title}`
  }
  next()
})

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount('#app')
