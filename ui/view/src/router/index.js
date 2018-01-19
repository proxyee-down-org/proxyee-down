import Vue from 'vue'
import Router from 'vue-router'
import NewTask from '../components/NewTask'
import TaskList from '../components/TaskList'
import ToolList from '../components/ToolList'
import BdyUnzip from '../components/tools/BdyUnzip'
import ConfigPage from '../components/ConfigPage'
import AboutPage from '../components/AboutPage'
import SupportPage from '../components/SupportPage'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: '/tasks'
    },
    {
      path: '/tasks',
      name: 'taskList',
      component: TaskList
    },
    {
      path: '/tasks/new/:id',
      name: 'newTask',
      component: NewTask
    },
    {
      path: '/tools',
      name: 'tools',
      component: ToolList
    },
    {
      path: '/tools/bdyUnzip',
      name: 'bdyUnzip',
      component: BdyUnzip
    },
    {
      path: '/config',
      name: 'config',
      component: ConfigPage
    },
    {
      path: '/about',
      name: 'about',
      component: AboutPage
    },
    {
      path: '/support',
      name: 'support',
      component: SupportPage
    }
  ]
})
