import Vue from 'vue'
import Vuex from 'vuex'
import tasks from './modules/tasks'
import tabs from './modules/tabs'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    tasks,
    tabs
  }
})
