import Vue from "vue";
import App from "./App.vue";
import router from "./router";
import store from "./store";
import iView from "iview";
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
