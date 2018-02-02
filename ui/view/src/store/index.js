import Vue from 'vue'
import Vuex from 'vuex'
import tasks from './modules/tasks'
import tabs from './modules/tabs'
import unzips from './modules/unzips'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    tasks,
    tabs,
    unzips,
  }
})
