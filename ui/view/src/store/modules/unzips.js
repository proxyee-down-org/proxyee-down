import Vue from 'vue'
import Util from '../../common/util'

export default {
  namespaced: true,
  state: {
    unzipTasks: [],
  },
  mutations: {
    setUnzipTask(state, task) {
      let index = Util.inArray(state.unzipTasks, task);
      if (index != -1) {
        Vue.set(state.unzipTasks, index, task);
      }else{
        state.unzipTasks.push(task);
      }
    }
  },
}

