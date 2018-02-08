export default {
  namespaced: true,
  state: {
    selectTab: '',
    tabs: [],
  },
  mutations: {
    setSelectTab(state, selectTab) {
      state.selectTab = selectTab;
    },
    addTab(state, tab) {
      state.tabs.push(tab);
    },
    delTab(state, index) {
      state.tabs.splice(index, 1);
    },
    setSecTitle(state, title) {
      state.tabs[state.selectTab].secTitle = title;
    },
  }
}

