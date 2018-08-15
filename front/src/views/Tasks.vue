<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed"
          icon="plus-round"
          class="tasks-button"
          @click="resolveVisible=true">{{$t("tasks.createTasks")}}</i-button>
        <i-button type="dashed"
          icon="ios-pause"
          class="tasks-button"
          @click="onPauseBatch">{{$t("tasks.pauseDownloads")}}</i-button>
        <i-button type="dashed"
          icon="ios-play"
          class="tasks-button"
          @click="onResumeBatch">{{$t("tasks.continueDownloading")}}</i-button>
        <i-button type="dashed"
          icon="ios-trash"
          class="tasks-button"
          @click="onDeleteBatch">{{$t("tasks.deleteTask")}}</i-button>
      </div>

      <Table :taskList="taskList"
        ref="taskTable"
        @on-delete="onDelete"
        @on-pause="onPause"
        @on-resume="onResume"
        @on-open="onOpen" />

      <Modal v-model="deleteModal"
        :title="$t('tasks.deleteTask')"
        @on-ok="doDelete(delTaskId)">
        <Checkbox :value="delFile"></Checkbox>
        <span @click="delFile=!delFile">{{$t('tasks.deleteTaskTip')}}</span>
      </Modal>

    </i-content>

    <Resolve v-model="resolveVisible" />
    <Create :request="createForm.request"
      :response="createForm.response"
      @close="$router.push('/');" />
  </div>
</template>

<script>
import Table from '../components/Table'
import Resolve from '../components/Task/Resolve'
import Create from '../components/Task/Create'
import { showFile } from '../common/native'

let intervalId

export default {
  name: 'tasks',
  components: {
    Table,
    Resolve,
    Create
  },
  mounted() {
    /**
     * 每秒读取下载进度
     */
    intervalId = setInterval(() => {
      if (!this.taskList) {
        return
      }
      //过滤出正在下载的任务ID
      const downloadingIds = this.taskList
        .filter(task => task.info.status == 1)
        .map(task => task.id)
      if (downloadingIds && downloadingIds.length) {
        this.$http
          .get('http://127.0.0.1:26339/tasks/progress?ids=' + downloadingIds)
          .then(result => {
            //匹配并更新正在下载的任务信息
            result.data.forEach(task => {
              const index = this.getIndexByTaskId(task.id)
              if (index >= 0) {
                this.taskList[index].info = task.info
              }
            })
          })
          .catch(error => {
            if (!error.response || error.response.status == 504) {
              clearInterval(intervalId)
            }
          })
      }
    }, 1000)
  },
  destroyed() {
    clearInterval(intervalId)
  },
  data() {
    return {
      taskList: [],
      deleteModal: false,
      delTaskId: '',
      delFile: false,
      resolveVisible: false,
      createForm: {
        request: null,
        response: null
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
    getAllTask() {
      this.$http
        .get('http://127.0.0.1:26339/tasks')
        .then(result => (this.taskList = result.data))
    },
    onRouteChange(query) {
      if (query.request && query.response) {
        this.createForm = {
          request: JSON.parse(query.request),
          response: JSON.parse(query.response)
        }
      } else {
        this.createForm = {
          request: null,
          response: null
        }
        this.getAllTask()
      }
    },
    onPause(task) {
      this.doPause(task.id)
    },
    onResume(task) {
      this.doResume(task.id)
    },
    onDelete(task) {
      this.delTaskId = task.id
      this.delFile = false
      this.deleteModal = true
    },
    onOpen(task) {
      showFile(task.config.filePath + '/' + task.response.fileName)
    },
    onPauseBatch() {
      this.doPause(this.getCheckedIds())
    },
    onResumeBatch() {
      this.doResume(this.getCheckedIds())
    },
    onDeleteBatch() {
      this.delTaskId = this.getCheckedIds()
      this.delFile = false
      this.deleteModal = true
    },
    doPause(ids) {
      this.$http.put('http://127.0.0.1:26339/tasks/' + ids + '/pause')
    },
    doResume(ids) {
      this.$http
        .put('http://127.0.0.1:26339/tasks/' + ids + '/resume')
        .then(result => {
          let pauseIds = result.data.pauseIds
          let resumeIds = result.data.resumeIds
          if (pauseIds) {
            pauseIds.forEach(pauseId => {
              const index = this.getIndexByTaskId(pauseId)
              if (index >= 0) {
                this.taskList[index].info.status = 2
              }
            })
          }
          if (resumeIds) {
            resumeIds.forEach(resumeId => {
              const index = this.getIndexByTaskId(resumeId)
              if (index >= 0) {
                this.taskList[index].info.status = 1
              }
            })
          }
        })
    },
    doDelete(ids) {
      this.$http
        .delete(
          'http://127.0.0.1:26339/tasks/' + ids + '?delFile=' + this.delFile
        )
        .then(() => {
          ids.split(',').forEach(id => {
            const index = this.getIndexByTaskId(id)
            if (index >= 0) {
              this.taskList.splice(index, 1)
            }
          })
        })
    },
    getCheckedIds() {
      return this.$refs['taskTable']
        .getCheckedTasks()
        .map(task => task.id)
        .join(',')
    },
    getIndexByTaskId(taskId) {
      return this.taskList.findIndex(t => t.id == taskId)
    }
  },
  created() {
    this.onRouteChange(this.$route.query)
  }
}
</script>

<style lang="less" scoped>
.tasks-entry {
  margin: 20px 0;
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
