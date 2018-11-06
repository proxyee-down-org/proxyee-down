<template>
  <div class="tasks">
    <div class="tasks-entry">
      <Button type="info"
        icon="plus"
        class="tasks-button"
        @click="resolveVisible=true">{{ $t("tasks.createTask") }}</Button>
      <Button type="warning"
        icon="ios-pause"
        class="tasks-button"
        @click="onPauseBatch">{{ $t("tasks.pauseDownloads") }}</Button>
      <Button type="warning"
        icon="ios-play"
        class="tasks-button"
        @click="onResumeBatch">{{ $t("tasks.continueDownloading") }}</Button>
      <Button type="error"
        icon="ios-trash"
        class="tasks-button"
        @click="onDeleteBatch">{{ $t("tasks.deleteTask") }}</Button>
    </div>
    <Tabs type="card"
      :animated="false"
      v-model="activeTab"
      style="overflow:visible;">
      <TabPane :label="$t('tasks.running')+'('+runList.length+')'"
        name="run"
        icon="play">
        <Table :taskList="runList"
          ref="runTable"
          :maxHeight="taskListMaxHeight"
          @on-delete="onDelete"
          @on-pause="onPause"
          @on-resume="onResume"
          @on-open="onOpen" />
      </TabPane>
      <TabPane :label="$t('tasks.waiting')+'('+waitList.length+')'"
        name="wait"
        icon="pause">
        <Table :taskList="waitList"
          ref="waitTable"
          :maxHeight="taskListMaxHeight"
          @on-delete="onDelete"
          @on-pause="onPause"
          @on-resume="onResume"
          @on-open="onOpen" />
      </TabPane>
      <TabPane :label="$t('tasks.done')+'('+doneList.length+')'"
        name="done"
        icon="checkmark">
        <Table :taskList="doneList"
          ref="doneTable"
          :maxHeight="taskListMaxHeight"
          @on-delete="onDelete"
          @on-pause="onPause"
          @on-resume="onResume"
          @on-open="onOpen" />
      </TabPane>
    </Tabs>

    <Modal v-model="deleteModal"
      :title="$t('tasks.deleteTask')">
      <Checkbox v-model="delFile"></Checkbox>
      <span @click="delFile=!delFile">{{ $t('tasks.deleteTaskTip') }}</span>
      <div slot="footer">
        <Button type="primary"
          @click="doDelete(delTaskIds)">{{ $t('tip.ok') }}</Button>
        <Button @click="deleteModal=false">{{ $t('tip.cancel') }}</Button>
      </div>
    </Modal>

    <Resolve v-model="resolveVisible" />
    <Create :request="createForm.request"
      :response="createForm.response"
      :config="createForm.config"
      :data="createForm.data"
      @close="$router.push('/');" />
  </div>
</template>

<script>
import Table from '../components/Table'
import Resolve from '../components/Task/Resolve'
import Create from '../components/Task/Create'
import { showFile } from '../common/native'
import ReconnectingWebSocket from 'reconnecting-websocket'

let ws

export default {
  name: 'tasks',
  components: {
    Table,
    Resolve,
    Create
  },

  mounted() {
    ws = new ReconnectingWebSocket('ws://' + window.location.hostname + ':26339/ws')
    ws.onmessage = evt => {
      const msg = eval('(' + evt.data + ')')
      const data = msg.data
      const updateTask = (taskIds, fromListArray, toList, handle) => {
        if (taskIds && taskIds.length) {
          taskIds.forEach(taskId => {
            fromListArray.forEach(fromList => {
              const index = fromList.findIndex(task => task.id == taskId)
              if (index >= 0) {
                const task = fromList[index]
                let moveFlag = false
                if (handle) {
                  moveFlag = handle(task)
                }
                //Move to the other task list
                if (moveFlag && fromList != toList) {
                  fromList.splice(index, 1)
                  toList.splice(0, 0, task)
                }
                this.refreshMaxHeight()
              }
            })
          })
        }
      }
      switch (msg.type) {
        case 'CREATE':
          if (data.info.status == 1) {
            this.runList.push(data)
            this.activeTab = 'run'
          } else {
            this.waitList.push(data)
            this.activeTab = 'wait'
          }
          this.refreshMaxHeight()
          break
        case 'PROGRESS':
          updateTask([data.id], [this.runList], this.doneList, task => {
            //Update the task progress info
            task.info = data.info
            return data.info.status == 4
          })
          break
        case 'PAUSE':
          updateTask(data, [this.runList, this.waitList], this.waitList, task => {
            //Update the task status to pause
            task.info.status = 2
            return true
          })
          this.activeTab = 'wait'
          break
        case 'ERROR':
          updateTask([data.id], [this.runList], this.waitList, task => {
            //Update the task status to error
            task.info.status = 3
            return true
          })
          this.activeTab = 'wait'
          break
        case 'RESUME':
          updateTask(data.pauseIds, [this.runList, this.waitList], this.waitList, task => {
            //Update the task status to pause
            task.info.status = 2
            return true
          })
          updateTask(data.waitIds, [this.runList, this.waitList], this.waitList, task => {
            //Update the task status to wait
            task.info.status = 0
            return true
          })
          updateTask(data.resumeIds, [this.waitList], this.runList, task => {
            //Update the task status to downloading
            task.info.status = 1
            return true
          })
          this.activeTab = 'run'
          break
        case 'DELETE': {
          let list = this[this.activeTab + 'List']
          if (list && data) {
            data.forEach(taskId => {
              const index = list.findIndex(task => task.id == taskId)
              if (index != -1) {
                list.splice(index, 1)
              }
            })
          }
          break
        }
      }
    }
    this.retryLoadTaskList()
  },

  destroyed() {
    ws.close()
  },

  data() {
    return {
      activeTab: 'run',
      taskListMaxHeight: undefined,
      runList: [],
      waitList: [],
      doneList: [],
      deleteModal: false,
      delTaskIds: [],
      delFile: false,
      resolveVisible: false,
      createForm: {
        request: null,
        response: null,
        config: null,
        data: null
      }
    }
  },

  watch: {
    $route() {
      this.onRouteChange(this.$route.query)
    }
  },

  methods: {
    showResolve() {
      this.resolveVisible = true
    },

    onRouteChange(query) {
      const flag = !!(query.request && query.response)
      this.createForm = {
        request: flag ? JSON.parse(query.request) : null,
        response: flag ? JSON.parse(query.response) : null,
        config: flag && query.config ? JSON.parse(query.config) : null,
        data: flag && query.data ? JSON.parse(query.data) : null
      }
    },

    onPause(task) {
      this.doPause([task.id])
    },

    onResume(task) {
      this.doResume([task.id])
    },

    onDelete(task) {
      this.delTaskIds = [task.id]
      this.delFile = false
      this.deleteModal = true
    },

    onOpen(task) {
      showFile(`${task.config.filePath}/${task.response.fileName}`)
    },

    onPauseBatch() {
      const ids = this.getCheckedIds()
      if (ids.length) {
        this.doPause(ids)
      }
    },

    onResumeBatch() {
      const ids = this.getCheckedIds()
      if (ids.length) {
        this.doResume(ids)
      }
    },

    onDeleteBatch() {
      const ids = this.getCheckedIds()
      if (ids.length) {
        this.delTaskIds = ids
        this.delFile = false
        this.deleteModal = true
      }
    },

    doPause(ids) {
      this.$http.put('http://127.0.0.1:26339/tasks/pause', ids)
    },

    doResume(ids) {
      this.$http.put('http://127.0.0.1:26339/tasks/resume', ids)
    },

    doDelete(ids) {
      this.$http
        .post(`http://127.0.0.1:26339/tasks/delete?delFile=${this.delFile}`, ids)
        .finally(() => (this.deleteModal = false))
    },

    getCheckedIds() {
      return this.$refs[this.activeTab + 'Table'].getCheckedTasks().map(task => task.id)
    },

    getIndexByTaskId(taskId) {
      return this.taskList.findIndex(t => t.id === taskId)
    },

    getTop(e) {
      let offset = e.offsetTop
      if (e.offsetParent) {
        offset += this.getTop(e.offsetParent)
      }
      return offset
    },

    retryLoadTaskList() {
      this.$noSpinHttp
        .get('http://127.0.0.1:26339/tasks')
        .then(result => {
          result.data.forEach(task => {
            if (task.info.status == 1) {
              //Downloading tasks
              this.runList.push(task)
            } else if (task.info.status == 4) {
              //Completed task
              this.doneList.push(task)
            } else {
              this.waitList.push(task)
            }
          })
          this.refreshMaxHeight()
        })
        .catch(error => {
          if (!error.response) {
            setTimeout(this.retryLoadTaskList, 3000)
          }
        })
    },

    refreshMaxHeight() {
      const taskListTop = this.getTop(this.$refs[this.activeTab + 'Table'].$el.querySelector('div.tb-body'))
      const windowHeight = document.documentElement.clientHeight || document.body.clientHeight
      this.taskListMaxHeight = windowHeight - taskListTop - 25
    }
  },

  created() {
    window.onresize = () => {
      this.refreshMaxHeight()
    }
    this.onRouteChange(this.$route.query)
  }
}
</script>

<style lang="less" scoped>
.tasks-entry {
  margin-bottom: 20px;
  .tasks-button {
    margin-right: 10px;
    &:last-of-type {
      margin-right: 0;
    }
  }
}

.ivu-table {
  td {
    background-color: inherit;
  }
}
</style>

<style lang="less">
.tasks {
  .ivu-table {
    .taskList {
      // background-color: yellow;
    }
    td {
      background-color: inherit;
    }
  }
}
</style>
