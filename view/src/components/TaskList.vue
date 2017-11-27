<template>
  <div v-if="tasks.length>0">
    <el-row type="flex" justify="center">
      <el-col :span="20">
        <el-row v-for="row in Math.ceil(tasks.length/cellSize)" :key="row">
          <el-col :span="8" v-for="task in rowTasks(row)" class="task-list-container" :key="row.id">
            <el-popover
              placement="right"
              title="下载详情"
              width="500"
              trigger="click">
              <ul class="task-list">
                <li v-for="chunk in task.chunkInfoList">
                  <el-progress :text-inside="true" :stroke-width="18"
                               :percentage="chunkProgress(chunk)"
                               :status="status(chunk)"></el-progress>
                  <span>{{chunkSpeed(chunk)}}kb/s</span>
                  <!--<span>{{chunk.totalSize}}byte</span>-->
                </li>
              </ul>
              <el-progress type="circle"
                           :percentage="progress(task)"
                           :status="status(task)"
                           :width="200"
                           slot="reference"></el-progress>
            </el-popover>
            <div>
              <p>{{speed(task)}}kb/s</p>
              <p>{{task.fileName}}</p>
              <p>{{task.fileSize}}byte</p>
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
  import Vue from 'vue'

  export default {
    name: 'taskList',
    data() {
      return {
        tasks: [],
        cellSize: 3,
        ws: new WebSocket('wss://localhost:8443/ws/progress'),
      }
    },
    methods: {
      taskIndex(i, j) {
        return (i - 1) * this.cellSize + j - 1;
      },
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
      fileDownSize(task) {
        let chunkInfoList = task.chunkInfoList;
        if (chunkInfoList && chunkInfoList.length > 0) {
          return chunkInfoList.map((chunk) => {
            return chunk.downSize;
          }).reduce((chunk1, chunk2) => {
            return chunk1 + chunk2;
          });
        }
        return 0;
      },
      progress(task) {
        let fileDownSize = this.fileDownSize(task);
        let fileTotalSize = task.fileSize;
        if (fileDownSize && fileTotalSize) {
          return Math.floor(fileDownSize * 100 / fileTotalSize);
        }
        return 0;
      },
      speed(task) {
        if (task.lastTime) {
          //下载了多少秒
          let usedTime = (task.lastTime - task.startTime) / 1000;
          //下载了多少kb
          let fileDownSize = this.fileDownSize(task) / 1024;
          //计算下载速度kb/s
          return Math.floor(fileDownSize / usedTime);
        }
        return 0;
      },
      chunkProgress(chunk) {
        let fileDownSize = chunk.downSize;
        let fileTotalSize = chunk.totalSize;
        if (fileDownSize && fileTotalSize) {
          return Math.floor(fileDownSize * 100 / fileTotalSize);
        }
        return 0;
      },
      chunkSpeed(chunk) {
        if (chunk.lastTime) {
          //下载了多少秒
          let usedTime = (chunk.lastTime - chunk.startTime) / 1000;
          //下载了多少kb
          let fileDownSize = chunk.downSize / 1024;
          //计算下载速度kb/s
          return Math.floor(fileDownSize / usedTime);
        }
        return 0;
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
      }
    },
    created() {
      /*this.tasks.push({
        fileName: 'test.txt',
        fileSize: 90000,
        supportRange: true,
        connections: 8,
        filePath: 'f:/',
        startTime: 0,
        lastTime: 0,
        status: 1,
        chunkInfoList: [{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},{speed:100,percentage:20},]
      });
      this.tasks.push({
        fileName: 'test.txt',
        fileSize: 90000,
        supportRange: true,
        connections: 8,
        filePath: 'f:/',
        startTime: 0,
        lastTime: 0,
        status: 1,
        chunkInfoList: []
      });
      this.tasks.push({
        fileName: 'test.txt',
        fileSize: 90000,
        supportRange: true,
        connections: 8,
        filePath: 'f:/',
        startTime: 0,
        lastTime: 0,
        status: 1,
        chunkInfoList: []
      });
      this.tasks.push({
        fileName: 'test.txt',
        fileSize: 90000,
        supportRange: true,
        connections: 8,
        filePath: 'f:/',
        startTime: 0,
        lastTime: 0,
        status: 1,
        chunkInfoList: []
      });
      this.tasks.push({
        fileName: 'test.txt',
        fileSize: 90000,
        supportRange: true,
        connections: 8,
        filePath: 'f:/',
        startTime: 0,
        lastTime: 0,
        status: 1,
        chunkInfoList: []
      });*/
      this.$http.get("api/getTaskList")
      .then((response) => {
        if (response.data && response.data.length) {
          response.data.forEach((task) => {
            this.tasks.push(task);
          });
        }
      })
      this.ws.onmessage = (e) => {
        var msg = eval('(' + e.data + ')');
        if (msg.type != 'start') {
          this.tasks.forEach((task, index) => {
            if (task.id == msg.taskInfo.id) {
              Vue.set(this.tasks, index, msg.taskInfo);
              return false;
            }
          });

        }
      }
    },
    destroyed() {
      this.ws.close();
    },
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
    width: 70%;
  }

  .task-list li > span {
    padding-left: 20px;
    float: right;
  }
</style>
