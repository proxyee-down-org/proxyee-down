<template>
  <div v-if="initFlag" v-loading="initFlag" style="height: 500px"
       element-loading-background="rgba(0, 0, 0, 0)">
  </div>
  <div v-else-if="tasks.length>0">
    <el-row type="flex" justify="center">
      <el-col :span="20">
        <el-row v-for="row in Math.ceil(tasks.length/cellSize)" :key="row">
          <el-col :span="8" v-for="task in rowTasks(row)" class="task-list-container"
                  :key="task.id">
            <el-popover
              placement="right-end"
              title="下载详情"
              width="400"
              trigger="click">
              <div class="file-detail">
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
                  <span>速度：</span>
                  <b>{{sizeFmt(speed(task), '0B')}}/s</b>
                </p>
                <p>
                  <span>状态：</span>
                  <b>{{leftTime(task)}}</b>
                </p>
              </div>
              <ul :class="{'task-list':true,'task-list-scroll':task.chunkInfoList.length>=16}">
                <li v-for="chunk in task.chunkInfoList" :key="chunk.index">
                  <task-progress :text-inside="true" :stroke-width="18"
                                 :percentage="task.totalProgress||progress(chunk)"
                                 :status="status(chunk)"></task-progress>
                  <span>{{sizeFmt(speed(chunk), '0B')}}/s</span>
                </li>
              </ul>
              <task-progress type="circle"
                             :percentage="task.totalProgress||progress(task)"
                             :status="status(task)"
                             :width="200"
                             slot="reference"></task-progress>
            </el-popover>
            <div class="task-progress-icon">
              <i v-if="task.status!=2"
                 :class="iconClass(task)"
                 @click="controlTask(task)"></i>
              <i v-if="task.status!=5" class="el-icon-task-delete" @click="deleteTask(task)"></i>
            </div>
            <p>{{task.fileName}}</p>
          </el-col>
        </el-row>
      </el-col>
    </el-row>
  </div>
  <div v-else>
    <p>暂无下载任务</p>
  </div>
</template>

<script>
  import Util from '../common/util'
  import TaskProgress from './base/TaskProgress'
  import Vue from 'vue'

  export default {
    data() {
      return {
        tasks: [],
        cellSize: 3,
        ws: new WebSocket('ws://' + window.location.host + '/ws/onProgress'),
        initFlag: true,
      }
    },
    components: {
      TaskProgress
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
      speed(task) {
        if (task.lastTime) {
          //下载了多少秒
          let usedTime = (task.lastTime - task.startTime - task.pauseTime) / 1000;
          //计算下载速度
          return Math.floor(task.downSize / usedTime)
        }
        return 0;
      },
      leftTime(task) {
        if (task.status == 7) {
          return '已完成';
        }
        if (task.status == 5) {
          return '暂停中';
        }
        let speed = this.speed(task);
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
          case 2:
          case 6:
            return 'exception';
          case 5:
            return 'warn';
          default:
            return null;
        }
      },
      sizeFmt(size, def) {
        return Util.sizeFmt(size, def);
      },
      iconClass(task) {
        if (task.status == 5) {
          return 'el-icon-task-start';
        } else {
          return 'el-icon-task-pause';
        }
      },
      controlTask(task) {
        if (task.status == 5) {
          this.$http.get('api/continueTask?id=' + task.id)
          .then((response) => {
            let result = response.data;
            if (result.status != 200) {
              this.$message(result.msg);
            }
          });
        } else {
          this.$http.get('api/pauseTask?id=' + task.id)
          .then((response) => {
            let result = response.data;
            if (result.status != 200) {
              this.$message(result.msg);
            }
          });
        }
      },
      deleteTask(task) {
        this.$confirm('确定删除该任务吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$http.get('api/deleteTask?id=' + task.id)
          .then((response) => {
            let result = response.data;
            if (result.status != 200) {
              this.$message(result.msg);
            }
          });
        });
      },
    },
    created() {
      this.ws.onopen = (e) => {
        if (this.initFlag) {
          this.initFlag = false;
        }
      };
      this.ws.onmessage = (e) => {
        let msg = eval('(' + e.data + ')');
        if (msg) {
          this.tasks = msg.sort((task1, task2) => {
            return task1.startTime - task2.startTime;
          });
          /*this.msg.forEach((task1)=>{
            this.tasks.forEach((task2, index2) => {
              if (task2.id == task1.id) {
                /!*msg.taskInfo.intervalTime = msg.taskInfo.lastTime - task.lastTime;
                msg.taskInfo.intervalDownSize = msg.taskInfo.downSize - task.downSize;
                msg.taskInfo.chunkInfoList.forEach((chunk,index) => {
                  chunk.intervalTime = chunk.lastTime - task.chunkInfoList[index].lastTime;
                  chunk.intervalDownSize = chunk.downSize - task.chunkInfoList[index].downSize;
                });*!/
                Vue.set(this.tasks, index2, task1);
                return false;
              }
            });
          });*/
        }
      };
    }
    ,
    destroyed() {
      this.ws.close();
    }
    ,
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
  .task-progress-icon {
    height: 60px;
  }

  .task-progress-icon i {
    padding: 10px 30px;
    font-size: 30px;
    cursor: pointer;
  }
</style>
