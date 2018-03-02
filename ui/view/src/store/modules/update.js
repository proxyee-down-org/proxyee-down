export default {
  namespaced: true,
  state: {
    updateTask: null,
  },
  mutations: {
    setUpdateTask(state, updateTask) {
      state.updateTask = updateTask;
    }
  },
}

