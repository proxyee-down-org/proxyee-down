import Vue from 'vue'
import Router from 'vue-router'
import NewTask from '../components/NewTask'
import TaskList from '../components/TaskList'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'taskList',
      component: TaskList
    },
    {
      path: '/newTask/:id',
      name: 'newTask',
      component: NewTask
    }
  ]
})
