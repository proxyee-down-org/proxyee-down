<template>
  <div id="app" style="height: 100%">
    <el-container style="height: 100%">
      <el-aside style="width: 120px;height: 100%">
        <el-menu
          :default-active="tabs[selectTab].uri"
          class="el-menu-vertical-demo"
          @select="openTabHandle(arguments[0])"
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
                   @tab-click="toTabHandle"
                   @tab-remove="delTabHandle"
                   closable>
            <el-tab-pane
              v-for="(tab,index) in tabs"
              :key="index"
              :name="index+''"
              :label="menus[tab.uri].title+(tab.secTitle?'-'+tab.secTitle:'')"
            ></el-tab-pane>
          </el-tabs>
        </el-header>
        <el-main>
          <component v-for="(tab,index) in tabs"
                     :key="index"
                     :is="menus[tab.uri].com"
                     v-bind="tab.args"
                     v-show="index==selectTab"></component>
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
  import {mapState, mapMutations} from 'vuex'

  export default {
    name: 'app',
    components: {
      TaskList, ToolList, ConfigPage, AboutPage, SupportPage
    },
    data() {
      return {
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
      }
    },
    computed: {
      selectTab: {
        get: function () {
          return this.$store.state.tabs.selectTab;
        },
        set: function (newValue) {
          this.$store.commit("tabs/setSelectTab", newValue)
        }
      },
      ...mapState('tabs', [
          'tabs',
        ],
      ),
      ...mapState('tasks', [
          'newTaskId',
        ],
      )
    },
    methods: {
      openTabHandle(index, args) {
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
            this.setSelectTab(matchIndex + "");
            return;
          }
        }
        this.setSelectTab(this.tabs.length + '');
        this.addTab({uri: index, args: args ? args : null, secTitle: ''});
      },
      toTabHandle(tab) {
        this.setSelectTab(tab.$data.index);
      },
      delTabHandle(index) {
        if (this.tabs.length > 1) {
          this.delTab(index);
          let selectIndex = parseInt(this.selectTab);
          if (selectIndex + 1 > this.tabs.length) {
            this.setSelectTab((selectIndex - 1) + '');
          }
        }
      },
      ...mapMutations('tabs', [
        'setSelectTab',
        'addTab',
        'delTab',
      ]),
      ...mapMutations('tasks', [
        'setTasks',
        'setNewTaskStatus',
        'setNewTaskId',
      ]),
      ...mapMutations('unzips', [
        'setUnzipTask',
      ]),
      ...mapMutations('update', [
        'setUpdateTask',
      ]),
    },
    created() {
      this.openTabHandle('/tasks');
      const ws = new WebSocket('ws://' + window.location.host + '/ws/onProgress');
      ws.onmessage = e => {
        let wsForm = eval('(' + e.data + ')');
        if (wsForm) {
          let data = wsForm.data;
          switch (wsForm.type) {
            case 1: //刷新任务列表
              this.setTasks(data);
              break;
            case 2: //解压进度
              this.setUnzipTask(data);
              break;
            case 3: //创建自动解压任务
              this.openTabHandle('/tools', {
                selectTool: 'BdyUnzip',
                args: {filePath: data.filePath, toPath: data.toPath}
              });
              break;
            case 4: //更新进度
              this.setUpdateTask(data);
              break;
          }
        }
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

  @import "./assets/icon/iconfont.css";
</style>

<style>
  .item {
    padding-left: 5px;
  }
</style>
