import Vue from 'vue'
import Vuex from 'vuex'
import tasks from './modules/tasks'
import tabs from './modules/tabs'
import unzips from './modules/unzips'
import update from './modules/update.js'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    tasks,
    tabs,
    unzips,
    update,
  }
})
