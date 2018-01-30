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
                  <b>{{sizeFmt(speedTask(task), '0B')}}/s</b>
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
                  <span>{{sizeFmt(speedChunk(chunk), '0B')}}/s</span>
                </li>
              </ul>
              <task-progress type="circle"
                             :percentage="task.totalProgress||progress(task)"
                             :status="status(task)"
                             :width="200"
                             slot="reference"></task-progress>
            </el-popover>
            <div class="task-progress-icon">
              <div v-if="task.status!=8">
                <i v-if="task.status!=7"
                   :class="iconClass(task)"
                   @click="controlTask(task)"></i>
                <i class="el-icon-task-delete" @click="deleteTask(task)"></i>
              </div>
              <div v-else>
                <i class="el-icon-loading"></i>
              </div>
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
        if (task.status == 8) {
          return '合并中';
        }
        if (task.status == 9) {
          return '待合并';
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
          case 8:
            return 'merge';
          case 2:
          case 6:
            return 'exception';
          case 5:
          case 9:
            return 'pause';
          case 1:
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
          .then((response) => {
            let result = response.data;
            if (result.status != 200) {
              this.$message(result.msg);
            }
          });
        }).catch(() => {
        });
      },
    },
    created() {
      this.ws.onmessage = (e) => {
        if (this.initFlag) {
          this.initFlag = false;
        }
        let msg = eval('(' + e.data + ')');
        if (msg) {
          this.tasks = msg.map((task1) => {
            this.tasks.forEach((task2) => {
              if (task2.id == task1.id) {
                task1.chunkInfoList.forEach((chunk, index) => {
                  chunk.intervalTime = chunk.lastTime - task2.chunkInfoList[index].lastTime;
                  chunk.intervalDownSize = chunk.downSize - task2.chunkInfoList[index].downSize;
                  chunk.speedCount = task2.chunkInfoList[index].speedCount;
                  if (chunk.intervalDownSize == 0) {
                    if (!chunk.speedCount) {
                      chunk.speedCount = 1;
                    } else {
                      chunk.speedCount++;
                    }
                  } else {
                    chunk.speedCount = 1;
                  }
                });
              }
              return false;
            });
            return task1;
          }).sort((task1, task2) => {
            return task2.startTime - task1.startTime;
          });
        }
      };
    },
    destroyed() {
      this.ws.close();
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
  .task-progress-icon {
    height: 60px;
  }

  .task-progress-icon i {
    padding: 10px 30px;
    font-size: 30px;
    cursor: pointer;
  }
</style>
