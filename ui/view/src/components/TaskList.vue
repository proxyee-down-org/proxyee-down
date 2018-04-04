<template>
  <div>
    <el-row v-if="!initFlag">
      <el-col :span="2">
        <el-tooltip content="创建任务" placement="right">
          <el-button class="el-icon-plus" @click="setNewTaskStatus(1)"></el-button>
        </el-tooltip>
      </el-col>
      <el-col :span="2" :offset="20">
        <el-tooltip content="视图模式" placement="right">
          <el-radio-group v-model="view">
            <el-radio-button label="list">列表</el-radio-button>
            <el-radio-button label="grid">格子</el-radio-button>
          </el-radio-group>
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
         style="height: 500px"
         element-loading-background="rgba(0, 0, 0, 0)">
    </div>
    <div v-else-if="tasks.length>0">
      <el-row type="flex" justify="center">
        <el-col :span="20">
          <el-row v-if="view=='list'"
                  class="task-list-row"
                  :gutter="20">
            <el-col :span="2">
              <el-checkbox border>全选</el-checkbox>
            </el-col>
            <el-col :span="6">
              <b>名称</b>
            </el-col>
            <el-col :span="2">
              <b>大小</b>
            </el-col>
            <el-col :span="8">
              <b>进度</b>
            </el-col>
            <el-col :span="2">
              <b>速度</b>
            </el-col>
            <el-col :span="2">
              <b>状态</b>
            </el-col>
            <el-col :span="2">
              <b>操作</b>
            </el-col>
          </el-row>
          <el-row v-for="(task,index) in view=='list'?tasks:Math.ceil(tasks.length/cellSize)"
                  :class="{'task-list-row':view=='list'}"
                  :gutter="view=='list'?20:0"
                  :key="index">
            <template v-if="view=='list'">
              <el-col :span="2">
                <el-checkbox border>选择</el-checkbox>
              </el-col>
              <el-col :span="6">
                <span>{{task.fileName}}</span>
              </el-col>
              <el-col :span="2">
                <span>{{sizeFmt(task.totalSize, '未知大小')}}</span>
              </el-col>
            </template>
            <el-col :span="8"
                    v-for="task in view=='list'?[task]:rowTasks(index+1)"
                    class="task-list-container"
                    :key="task.id+view">
              <el-popover
                placement="right-end"
                title="下载详情"
                width="400"
                trigger="click">
                <div class="file-detail">
                  <p>
                    <b style="display:block;height: 40px;overflow-y: auto">{{task.url}}</b>
                  </p>
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
                  </p>
                </div>
                <ul :class="{'task-list':true,'task-list-scroll':task.chunkInfoList.length>=16}">
                  <li v-for="chunk in task.chunkInfoList" :key="chunk.index">
                    <task-progress :text-inside="true" :stroke-width="18"
                                   :percentage="task.totalProgress||progress(chunk)"
                                   :status="status(chunk)"></task-progress>
                    <span>{{sizeFmt(speedChunk(chunk), '0B')}}/s</span>
                  </li>
                </ul>
                <task-progress v-if="view=='list'"
                               :text-inside="true"
                               :stroke-width="30"
                               :percentage="task.totalProgress||progress(task)"
                               :status="status(task)"
                               slot="reference"></task-progress>
                <task-progress v-else
                               type="circle"
                               :percentage="task.totalProgress||progress(task)"
                               :status="status(task)"
                               slot="reference"
                               :width="200"></task-progress>
              </el-popover>
              <div v-if="view!='list'" class="task-grid-icon">
                <i v-if="task.status!=7"
                   :class="iconClass(task)"
                   @click="controlTask(task)"></i>
                <i class="el-icon-task-delete" @click="deleteTask(task)"></i>
                <i v-if="task.status==7" class="el-icon-task-folder"
                   @click="openTaskDir(task)"></i>
                <p>{{task.fileName}}</p>
              </div>
            </el-col>
            <template v-if="view=='list'">
              <el-col :span="2">
                <span>{{sizeFmt(speedTask(task), '0B')}}/s</span>
              </el-col>
              <el-col :span="2">
                <span>{{leftTime(task)}}</span>
              </el-col>
              <el-col :span="2">
                <div class="task-list-icon">
                  <i v-if="task.status!=7"
                     :class="iconClass(task)"
                     @click="controlTask(task)"></i>
                  <i class="el-icon-task-delete" @click="deleteTask(task)"></i>
                  <i v-if="task.status==7" class="el-icon-task-folder"
                     @click="openTaskDir(task)"></i>
                </div>
              </el-col>
            </template>
          </el-row>
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
  import ElCol from "element-ui/packages/col/src/col";
  import ElRow from "element-ui/packages/row/src/row";
  import ElCheckbox from "../../node_modules/element-ui/packages/checkbox/src/checkbox.vue";

  export default {
    components: {
      ElCheckbox,
      ElRow,
      ElCol,
      BuildTask,
      NewTask,
      TaskProgress,
      NativeA
    },
    data() {
      return {
        view: 'list'
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
      rowTasks(row) {
        let ret = [];
        let start = (row - 1) * this.cellSize;
        for (let i = 0; i < this.cellSize; i++) {
          if (start + i == this.tasks.length) {
            break;
          }
          ret.push(this.tasks[start + i]);
        }
        return ret;
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
        if (task.status == 6) {
          return '失败';
        }
        if (task.status == 7) {
          return '已完成';
        }
        if (task.status == 5) {
          return '暂停中';
        }
        let speed = this.speedTask(task);
        if (speed) {
          return Util.timeFmt((task.totalSize - task.downSize) / speed);
        } else {
          return '未知';
        }
      },
      status(task) {
        switch (task.status) {
          case 7:
            return 'success';
          case 6:
            return 'exception';
          case 5:
            return 'pause';
          case 1:
          case 2:
          case 8:
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
        if (task.status == 5 || task.status == 6 || task.status == 9) {
          this.$http.get('api/continueTask?id=' + task.id)
          .then(() => {
          }).catch(() => {
          });
        } else {
          this.$http.get('api/pauseTask?id=' + task.id)
          .then(() => {
          }).catch(() => {
          });
        }
      },
      deleteTask(task) {
        const check = document.getElementById("task-delete");
        if (check) {
          document.getElementById("task-delete").checked = false;
        }
        this.$confirm(
          '<label>' +
          '<input id="task-delete" type="checkbox" style="height:18px;width:18px;vertical-align:middle;">'
          +
          '<span>删除任务和文件</span>' +
          '</label>',
          '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            dangerouslyUseHTMLString: true
          }).then(() => {
          this.$http.get('api/deleteTask?id=' + task.id + "&delFile=" + document.getElementById(
            "task-delete").checked)
          .then(() => {
            this.$store.commit("tasks/delTask", task.id);
          }).catch(() => {
          });
        }).catch(() => {
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
  .task-list-container {
    padding-bottom: 30px;
    text-align: center;
  }

  .task-list {
    margin: 0px;
    padding: 0px;
  }

  .task-list-scroll {
    overflow-y: auto;
    height: 448px;
  }

  .task-list li {
    list-style: none;
    padding-bottom: 8px;
  }

  .task-list li > div {
    display: inline-block;
    width: 70%;
  }

  .task-list li > span {
    padding-left: 20px;
    padding-right: 5px;
    float: right;
  }

  .file-detail {
    font-size: 15px;
  }

  @import "../assets/icon/iconfont.css";

  .task-grid-icon {
    height: 40px;
  }

  .task-grid-icon i {
    padding: 10px 30px;
    font-size: 30px;
    cursor: pointer;
  }

  .task-list-row {
    padding-top: 30px;
    text-align: center;
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

  .task-list-file {
    position: relative;
    top: -25px;
  }

  .task-list-file > span {
    display: block;
    font-size: 18px;
    padding-top: 10px;
    color: #409eff;
  }

  .task-list-file-name {

  }

  .task-list-file-size {

  }
</style>
