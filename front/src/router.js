import Vue from 'vue'
import Router from 'vue-router'
import Tasks from './views/Tasks.vue'
import Extension from './views/Extension.vue'
import Setting from './views/Setting.vue'
import About from './views/About.vue'
import Support from './views/Support.vue'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      redirect: '/tasks'
    },
    {
      path: '/tasks',
      name: 'tasks',
      component: Tasks
    },
    {
      path: '/extension',
      name: 'extension',
      component: Extension
    },
    {
      path: '/setting',
      name: 'setting',
      component: Setting
    },
    {
      path: '/about',
      name: 'About',
      component: About
    },
    {
      path: '/support',
      name: 'Support',
      component: Support
    }
  ]
})
