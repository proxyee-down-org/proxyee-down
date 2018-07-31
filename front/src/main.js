import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import iView from "iview";
import axios from "axios";
import VueI18n from "vue-i18n";

import "iview/dist/styles/iview.css";
import en from "iview/dist/locale/en-US";
import zh from "iview/dist/locale/zh-CN";
import { getLanguage } from "./common/utils";

Vue.use(VueI18n);
Vue.use(iView);

Vue.config.productionTip = false;

// 设置i18n
// setting i18n
const i18n = new VueI18n({
  locale: getLanguage(),
  messages: {
    en: Object.assign(require("./i18n/en").default, en),
    zh: Object.assign(require("./i18n/zh").default, zh)
  }
});

Vue.prototype.$http = axios.create();
Vue.prototype.$http.interceptors.response.use(
  response => {
    return response;
  },
  error => {
    if (error.response.status == 504) {
      Vue.prototype.$Message.error(i18n.t("alert.timeout"));
    } else {
      Vue.prototype.$Message.error(i18n.t("alert.error"));
    }
    return Promise.reject(error);
  }
);

Promise.prototype.finally = function(callback) {
  let P = this.constructor;
  return this.then(
    value => P.resolve(callback()).then(() => value),
    reason =>
      P.resolve(callback()).then(() => {
        throw reason;
      })
  );
};

// 路由发生变化修改页面title
// change the page according to the routing changes title
router.beforeEach((to, from, next) => {
  if (to.meta.title) {
    document.title = `ProxyeeDown-${to.meta.title}`;
  }
  next();
});

new Vue({
  router,
  store,
  i18n,
  render: h => h(App)
}).$mount("#app");
