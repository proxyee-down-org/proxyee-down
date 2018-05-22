<template>
  <div>
    <el-row v-if="!initFlag">
      <el-col>
        <el-tooltip content="创建任务">
          <el-button class="el-icon-plus tool-button" @click="setNewTaskStatus(1)"></el-button>
        </el-tooltip>
        <el-tooltip content="继续任务">
          <el-button class="el-icon-task-start tool-button" @click="continueAllHandle"></el-button>
        </el-tooltip>
        <el-tooltip content="暂停任务">
          <el-button class="el-icon-task-pause tool-button" @click="pauseAllHandle"></el-button>
        </el-tooltip>
        <el-tooltip content="删除任务">
          <el-button class="el-icon-task-delete tool-button" @click="deleteAllHandle"></el-button>
        </el-tooltip>
      </el-col>
    </el-row>
    <el-dialog
      :title="newTaskTitle"
      :visible="newTaskStatus>0"
      :show-close="false"
      :append-to-body="true"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :before-close="dialogCloseHandle">
      <build-task v-if="newTaskStatus==1"
                  @onSubmit="buildTaskHandle"
                  @onCancel="dialogCloseHandle"></build-task>
      <new-task v-if="newTaskStatus==2"
                :taskId="newTaskId"
                @onSubmit="dialogCloseHandle"
                @onCancel="dialogCloseHandle"></new-task>
    </el-dialog>
    <div v-if="initFlag"
         v-loading="initFlag"
         style="height: 500px">
    </div>
    <div v-else-if="tasks.length>0">
      <el-row class="task-list-row task-list-row-title"
              :gutter="20">
        <el-col :span="1">
          <el-checkbox @change="checkAllHandle" v-model="checkAll" :indeterminate="checkSome">
            &nbsp;
          </el-checkbox>
        </el-col>
        <el-col :span="6">
          <b>名称</b>
        </el-col>
        <el-col :span="3">
          <b>大小</b>
        </el-col>
        <el-col :span="5">
          <b>进度</b>
        </el-col>
        <el-col :span="3">
          <b>速度</b>
        </el-col>
        <el-col :span="3">
          <b>状态</b>
        </el-col>
        <el-col :span="3">
          <b>操作</b>
        </el-col>
      </el-row>
      <el-row v-for="(task,index) in tasks"
              :gutter="20"
              class="task-list-row"
              :key="task.id">
        <el-col :span="1">
          <el-checkbox v-model="checkTasks" :label="task.id" @change="checkHandle">&nbsp;
          </el-checkbox>
        </el-col>
        <el-col :span="6">
          <el-tooltip :content="task.fileName">
            <p>{{task.fileName}}</p>
          </el-tooltip>
        </el-col>
        <el-col :span="3">
          <p>{{sizeFmt(task.totalSize, '未知大小')}}</p>
        </el-col>
        <el-col :span="5"
                class="task-list-container">
          <el-popover
            placement="right-end"
            title="下载详情"
            width="400"
            trigger="click">
            <div class="file-detail">
              <el-popover
                popper-class="file-detail-popper"
                :content="task.url"
                trigger="click">
                <p style="white-space: nowrap;text-overflow: ellipsis;overflow: hidden;"
                   slot="reference">
                  {{task.url}}
                </p>
              </el-popover>
              <p>
                <span>名称：</span>
                <b>{{task.fileName}}</b>
              </p>
              <p>
                <span>路径：</span>
                <b>{{task.filePath}}</b>
              </p>
              <p>
                <span>大小：</span>
                <b>{{sizeFmt(task.totalSize, '未知大小')}}</b>
              </p>
              <p>
                <span>分段：</span>
                <b>{{task.connections}}</b>
              </p>
              <p>
                <span>速度：</span>
                <b>{{sizeFmt(speedTask(task), '0B')}}/s</b>
              </p>
              <p>
                <span>状态：</span>
                <b>{{leftTime(task)}}</b>
              </p>
            </div>
            <ul
              :class="{'task-detail-list':true,'task-detail-list-scroll':task.chunkInfoList.length>=16}">
              <li v-for="chunk in task.chunkInfoList" :key="chunk.index">
                <task-progress :text-inside="true" :stroke-width="18"
                               :percentage="progress(chunk)"
                               :status="status(chunk)"></task-progress>
                <span>{{sizeFmt(speedChunk(chunk), '0B')}}/s</span>
              </li>
            </ul>
            <task-progress :text-inside="true"
                           :stroke-width="30"
                           :percentage="progress(task)"
                           :status="status(task)"
                           slot="reference"></task-progress>
          </el-popover>
        </el-col>
        <el-col :span="3">
          <p>{{sizeFmt(speedTask(task), '0B')}}/s</p>
        </el-col>
        <el-col :span="3">
          <el-tag :type="statusType(task)">{{leftTime(task)}}</el-tag>
          <el-tooltip v-show="task.status==6" class="item"
                      placement="right">
            <div slot="content">下载链接失效，可尝试
              <native-a
                href="https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/refresh/read.md"
                target="_blank" style="color: #3a8ee6">刷新下载链接
              </native-a>
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
          <el-tooltip v-show="task.status==4&&progress(task)>0&&speedTask(task)==0" class="item"
                      placement="right">
            <div slot="content">若长时间下载速度为0，可尝试
              <native-a
                href="https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/refresh/read.md"
                target="_blank" style="color: #3a8ee6">刷新下载链接
              </native-a>
            </div>
            <i class="el-icon-question"></i>
          </el-tooltip>
        </el-col>
        <el-col :span="3">
          <div class="task-list-icon">
            <i v-if="task.status==7" class="el-icon-task-folder"
               @click="openTaskDir(task)"></i>
            <i v-if="task.status!=7"
               :class="iconClass(task)"
               @click="controlTask(task)"></i>
            <i class="el-icon-task-delete" @click="deleteTask(task)"></i>
          </div>
        </el-col>
      </el-row>
    </div>
    <div v-else style="text-align: center;margin-top: 10%">
      <h1>暂无下载任务</h1>
    </div>
  </div>
</template>

<script>
  import Util from '../common/util'
  import NativeA from './base/NativeA'
  import BuildTask from './BuildTask'
  import NewTask from './NewTask'
  import TaskProgress from './base/TaskProgress'
  import {mapState, mapMutations} from 'vuex'

  export default {
    components: {
      BuildTask,
      NewTask,
      TaskProgress,
      NativeA
    },
    data() {
      return {
        checkTasks: [],
        checkAll: false,
        checkSome: false,
        urlShow: {},
      }
    },
    computed: {
      newTaskTitle() {
        if (this.newTaskStatus == 1) {
          return '创建任务';
        } else if (this.newTaskStatus == 2) {
          return '开始任务';
        }
        return null;
      },
      ...mapState('tasks', [
          'newTaskStatus',
          'tasks',
          'cellSize',
          'initFlag',
          'newTaskId',
        ],
      )
    },
    watch: {
      newTaskId() {
        this.setNewTaskStatus(0);
        //强制渲染
        this.$nextTick(() => {
          this.setNewTaskStatus(2);
        });
      }
    },
    methods: {
      checkAllHandle(isCheck) {
        if (isCheck) {
          this.checkTasks = this.tasks.map(task => task.id);
          this.checkSome = false;
        } else {
          this.checkTasks = [];
        }
      },
      checkHandle() {
        this.checkAll = this.checkTasks.length == this.tasks.length;
        this.checkSome = !this.checkAll && this.checkTasks.length > 0;
      },
      progress(task) {
        let fileDownSize = task.downSize;
        let fileTotalSize = task.totalSize;
        if (fileDownSize > 0 && fileTotalSize > 0) {
          return Math.floor(fileDownSize * 100 / fileTotalSize);
        }
        return 0;
      },
      speedTask(task) {
        if (task.status == 5 || task.status == 7 || task.status == 8 || task.status == 9) {
          return this.speedAvg(task);
        }
        return task.chunkInfoList.map((chunk) => {
          if (chunk.status == 7 || chunk.status == 5) {
            return 0;
          }
          return this.speedChunk(chunk);
        }).reduce((speed1, speed2) => {
          return speed1 + speed2;
        });
      },
      speedChunk(chunk) {
        if (chunk.status == 7 || chunk.status == 5) {
          return this.speedAvg(chunk);
        } else {
          let speed = this.speedInterval(chunk);
          if (chunk.speedCount > 5 || speed > 0) {
            return speed;
          } else {
            return this.speedAvg(chunk);
          }
        }
      },
      speedAvg(task) {
        if (task.lastTime) {
          //下载了多少秒
          let usedTime = (task.lastTime - task.startTime - task.pauseTime) / 1000;
          //计算下载速度
          return Math.floor(task.downSize / usedTime)
        }
        return 0;
      },
      speedInterval(task) {
        if (task.intervalTime) {
          //计算下载速度
          return Math.floor(task.intervalDownSize / (task.intervalTime / 1000));
        }
        return 0;
      },
      leftTime(task) {
        if (task.status == 5) {
          return '暂停中';
        }
        if (task.status == 6) {
          return '失败';
        }
        if (task.status == 7) {
          return '已完成';
        }
        let speed = this.speedTask(task);
        if (speed) {
          return Util.timeFmt((task.totalSize - task.downSize) / speed);
        } else {
          return '未知';
        }
      },
      statusType(task) {
        if (task.status == 5) {
          return 'info';
        }
        if (task.status == 6) {
          return 'danger';
        }
        if (task.status == 7) {
          return 'success';
        }
        if (!this.speedTask(task)) {
          return 'warning';
        }
        return null;
      },
      status(task) {
        switch (task.status) {
          case 7:
            return 'success';
          case 6:
          case 8:
            return 'exception';
          case 5:
            return 'pause';
          case 1:
          case 2:
            return 'ready';
          default:
            return null;
        }
      },
      sizeFmt(size, def) {
        return Util.sizeFmt(size, def);
      },
      iconClass(task) {
        if (task.status == 5 || task.status == 6 || task.status == 9) {
          return 'el-icon-task-start';
        } else {
          return 'el-icon-task-pause';
        }
      },
      controlTask(task) {
        let load = this.$loading();
        if (task.status == 5 || task.status == 6 || task.status == 9) {
          this.$http.get('api/continueTask?id=' + task.id)
          .then(() => {
            load.close();
          }).catch(() => {
          });
        } else {
          this.$http.get('api/pauseTask?id=' + task.id)
          .then(() => {
            load.close();
          }).catch(() => {
          });
        }
      },
      deleteTask(task) {
        this.deleteConfirm(checked => {
          let load = this.$loading();
          this.$http.get('api/deleteTask?id=' + task.id + "&delFile=" + checked)
          .then(() => {
            this.$store.commit("tasks/delTask", task.id);
            load.close();
          }).catch(() => {
          });
        });
      },
      openTaskDir(task) {
        this.$http.get('api/openTaskDir?id=' + task.id)
        .then(() => {
        }).catch(() => {
        });
      },
      dialogCloseHandle() {
        this.setNewTaskStatus(0);
      },
      buildTaskHandle(result) {
        if (result.data) {
          this.setNewTaskId(result.data);
          this.setNewTaskStatus(2);
        }
      },
      deleteConfirm(call) {
        const check = document.getElementById("task-delete");
        if (check) {
          document.getElementById("task-delete").checked = false;
        }
        this.$confirm(
          `<label>
          <input id="task-delete" type="checkbox" style="height:18px;width:18px;vertical-align:middle;">
          <span>删除任务和文件</span>
          </label>`,
          '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            dangerouslyUseHTMLString: true
          }).then(() => {
          call(document.getElementById("task-delete").checked);
        }).catch(() => {
        });
      },
      hasChecked() {
        if (this.checkTasks.length == 0) {
          this.$message({showClose: true, message: '请选择任务'});
          return false;
        }
        return true;
      },
      continueAllHandle() {
        if (this.hasChecked()) {
          let load = this.$loading();
          this.$http.post('api/continueAllTask', this.checkTasks)
          .then(() => {
            load.close();
          }).catch(() => {
          });
        }
      },
      pauseAllHandle() {
        if (this.hasChecked()) {
          let load = this.$loading();
          this.$http.post('api/pauseAllTask', this.checkTasks)
          .then(() => {
            load.close();
          }).catch(() => {
          });
        }
      },
      deleteAllHandle() {
        if (this.hasChecked()) {
          this.deleteConfirm(checked => {
            this.loadFlag = true;
            this.$http.post('api/deleteAllTask?delFile=' + checked, this.checkTasks)
            .then(() => {
              this.checkTasks.forEach(taskId => this.$store.commit("tasks/delTask", taskId));
              this.loadFlag = false;
            }).catch(() => {
            });
          });
        }
      },
      ...mapMutations('tasks', [
        'setNewTaskStatus',
        'setNewTaskId',
      ]),
    },
    created() {
      this.$http.get('api/getStartTasks')
      .then(result => {
        this.$store.commit("tasks/setTasks", result.data);
        this.$store.commit("tasks/setInitFlag", false);
      }).catch(() => {
      });
      this.$http.get('api/getNewTask')
      .then(result => {
        this.buildTaskHandle(result);
      }).catch(() => {
      });
      this.$notify.info({
        title: 'Tips',
        position: 'bottom-right',
        duration: 0,
        message: '点击进度条可以查看任务下载详情'
      });
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .tool-button {
    font-size: 14px;
    padding-right: 20px;
  }

  .task-list-container {
    padding-bottom: 30px;
    text-align: center;
  }

  .task-detail-list {
    margin: 0px;
    padding: 0px;
  }

  .task-detail-list-scroll {
    overflow-y: auto;
    height: 448px;
  }

  .task-detail-list li {
    list-style: none;
    padding-bottom: 8px;
  }

  .task-detail-list li > div {
    display: inline-block;
    width: 70%;
  }

  .task-detail-list li > span {
    padding-left: 20px;
    padding-right: 5px;
    float: right;
  }

  .file-detail {
    font-size: 15px;
  }

  @import "../assets/icon/iconfont.css";

  .task-list-row {
    text-align: center;
  }

  .task-list-row-title {
    padding-top: 30px;
    padding-bottom: 30px;
  }

  .task-list-row p {
    position: relative;
    top: -16px;
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }

  .task-list-icon {
    height: 40px;
    top: 0px;
    left: 0px;
  }

  .task-list-icon i {
    font-size: 30px;
    cursor: pointer;
    padding-left: 15px;
  }
</style>

<style>
  .el-checkbox__inner {
    width: 18px;
    height: 18px;
  }

  .el-checkbox__inner::after {
    left: 6px;
    height: 10px;
  }

  .file-detail-popper {
    padding: 10px;
    width: 60%;
    word-break: break-all;
    color: #ffffff;
    font-size: 12px;
    background: #303133;
  }
</style>
