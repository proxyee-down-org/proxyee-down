import Vue from 'vue'
import Router from 'vue-router'
import Tasks from './views/Tasks.vue'
import Extension from './views/Extension.vue'

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
      component: Tasks,
      meta: {
        title: '任务列表'
      }
    },
    {
      path: '/extension',
      name: 'extension',
      component: Extension,
      meta: {
        title: '扩展列表'
      }
    }
  ]
})
