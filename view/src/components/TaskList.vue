<template>
  <div v-if="tasks.length>0">
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
              <ul class="task-list">
                <li v-for="chunk in task.chunkInfoList" :key="chunk.index">
                  <el-progress :text-inside="true" :stroke-width="18"
                               :percentage="progress(chunk)"
                               :status="status(chunk)"></el-progress>
                  <span>{{sizeFmt(speed(chunk))}}/s</span>
                </li>
              </ul>
              <el-progress type="circle"
                           :percentage="progress(task)"
                           :status="status(task)"
                           :width="200"
                           slot="reference"></el-progress>
            </el-popover>
            <div class="file-detail">
              <p>{{sizeFmt(speed(task))}}/s</p>
              <p>{{leftTime(task)}}</p>
              <p>{{task.fileName}}</p>
              <p>{{sizeFmt(task.totalSize)}}</p>
            </div>
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
  import Vue from 'vue'

  export default {
    name: 'taskList',
    data() {
      return {
        tasks: [],
        cellSize: 3,
        ws: new WebSocket('ws://' + window.location.host + '/ws/progress'),
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
        if (fileDownSize && fileTotalSize) {
          return Math.floor(fileDownSize * 100 / fileTotalSize);
        }
        return 0;
      },
      speed(task) {
        if (task.lastTime) {
          //下载了多少秒
          let usedTime = (task.lastTime - task.startTime) / 1000;
          //计算下载速度
          return Math.floor(task.downSize / usedTime)
        }
        return 0;
        /*if (task.intervalTime) {
          return Math.floor(task.intervalDownSize / (task.intervalTime / 1000))
        }
        return 0;*/
      },
      leftTime(task) {
        if (task.status == 2) {
          return '已完成';
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
          case 2:
            return 'success';
          case 3:
            return 'exception';
          default:
            return null;
        }
      },
      sizeFmt(size) {
        return Util.sizeFmt(size);
      },
    },
    created() {
      this.$http.get('api/getTaskList')
      .then((response) => {
        if (response.data && response.data.length) {
          response.data.sort((task1, task2) => {
            return task1.startTime - task2.startTime;
          }).forEach((task) => {
            this.tasks.push(task);
          });
        }
      });
      this.ws.onmessage = (e) => {
        let msg = eval('(' + e.data + ')');
        if (msg.type != 'start') {
          this.tasks.forEach((task, index) => {
            if (task.id == msg.taskInfo.id) {
              /*msg.taskInfo.intervalTime = msg.taskInfo.lastTime - task.lastTime;
              msg.taskInfo.intervalDownSize = msg.taskInfo.downSize - task.downSize;
              msg.taskInfo.chunkInfoList.forEach((chunk,index) => {
                chunk.intervalTime = chunk.lastTime - task.chunkInfoList[index].lastTime;
                chunk.intervalDownSize = chunk.downSize - task.chunkInfoList[index].downSize;
              });*/
              Vue.set(this.tasks, index, msg.taskInfo);
              return false;
            }
          });

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
    text-align: center;
  }

  .task-list {
    margin: 0px;
    padding: 0px;
  }

  .task-list li {
    list-style: none;
    padding-bottom: 8px;
  }

  .task-list li > div {
    display: inline-block;
    width: 75%;
  }

  .task-list li > span {
    padding-left: 20px;
    float: right;
  }

  .file-detail {
    font-size: 15px;
  }
</style>
