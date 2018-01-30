<template>
  <div id="app" style="height: 100%">
    <el-container style="height: 100%">
      <el-aside style="width: 120px;height: 100%">
        <el-menu
          :default-active="tabs[selectTab].uri"
          class="el-menu-vertical-demo"
          @select="openTab"
          background-color="#545c64"
          text-color="#fff"
          active-text-color="#ffd04b"
          style="height: 100%">
          <el-menu-item
            v-for="(menu,key) in menus"
            :key="key"
            :index="key">
            <i :class="menu.icon"></i>
            <span slot="title">{{menu.title}}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <el-tabs v-model="selectTab"
                   type="card"
                   @tab-click="toTab"
                   @tab-remove="delTab"
                   closable>
            <el-tab-pane
              v-for="(tab,index) in tabs"
              :key="index"
              :name="index+''"
              :label="menus[tab.uri].title"
            ></el-tab-pane>
          </el-tabs>
        </el-header>
        <el-main>
          <div v-for="(tab,index) in tabs"
               :is="menus[tab.uri].com"
               v-show="index==selectTab"></div>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script>
  import TaskList from './components/TaskList'
  import ToolList from './components/ToolList'
  import ConfigPage from './components/ConfigPage'
  import AboutPage from './components/AboutPage'
  import SupportPage from './components/SupportPage'

  export default {
    name: 'app',
    components: {
      TaskList, ToolList, ConfigPage, AboutPage, SupportPage
    },
    data() {
      return {
        selectTab: null,
        menus: {
          '/tasks': {
            title: '任务',
            icon: 'el-icon-menu',
            com: 'TaskList',
          },
          '/tools': {
            title: '工具',
            icon: 'el-icon-task-tool',
            com: 'ToolList',
            repeat: true,
          },
          '/config': {
            title: '设置',
            icon: 'el-icon-setting',
            com: 'ConfigPage',
          },
          '/about': {
            title: '关于',
            icon: 'el-icon-info',
            com: 'AboutPage',
          },
          '/support': {
            title: '打赏',
            icon: 'el-icon-task-money',
            com: 'SupportPage',
          },
        },
        tabs: []
      }
    },
    methods: {
      openTab(index) {
        //只能打开一次
        if (!this.menus[index].repeat) {
          let matchIndex = -1;
          this.tabs.forEach((tab, i) => {
            if (tab.uri == index) {
              matchIndex = i;
              return;
            }
          });
          if (matchIndex >= 0) {
            this.selectTab = matchIndex + "";
            return;
          }
        }
        this.selectTab = this.tabs.length + '';
        this.tabs.push({uri: index});
      },
      toTab(tab) {
        this.selectTab = tab.$data.index;
      },
      delTab(index) {
        if (this.tabs.length > 1) {
          this.tabs.splice(index, 1);
          let selectIndex = parseInt(this.selectTab);
          if (selectIndex + 1 > this.tabs.length) {
            this.selectTab = (selectIndex - 1) + '';
          }
        }
      }
    },
    created() {
      this.openTab("/tasks");
      const ws = new WebSocket('ws://' + window.location.host + '/ws/onProgress');
      ws.onmessage = e => {
        this.$store.commit('tasks/setTasks', eval('(' + e.data + ')'))
      }
    }
  }
</script>

<style scoped>
  .el-aside {
    height: 100%;
  }

  .el-header {
    padding: 0;
  }

  .el-main {
  }

  @import "./assets/icon/iconfont.css";
</style>
