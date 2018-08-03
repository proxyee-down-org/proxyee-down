<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed"
          icon="plus-round"
          class="tasks-button"
          @click="resolveVisible=true">{{$t("tasks.createTasks")}}</i-button>
        <i-button type="dashed"
          icon="ios-play"
          class="tasks-button">{{$t("tasks.continueDownloading")}}</i-button>
        <i-button type="dashed"
          icon="ios-pause"
          class="tasks-button">{{$t("tasks.pauseDownloads")}}</i-button>
        <i-button type="dashed"
          icon="ios-trash"
          class="tasks-button">{{$t("tasks.deleteTask")}}</i-button>
      </div>

      <Table :data="data"
        progress
        @on-check-change="onCheckChange"
        @on-select-all="onSelectAll" />

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

export default {
  name: 'tasks',
  components: {
    Table,
    Resolve,
    Create
  },
  mounted() {
    // this.taskProgress();
    let arr1 = [22, 31, 66, 71, 22, 100, 96, 56, 78, 97]
    let arr2 = ['10KB/s', '22M/s', '2.2B/s', '6G/s']

    setInterval(() => {
      this.data.forEach(item => {
        item.progress = arr1[parseInt(Math.random() * arr1.length)]
        item.downloadSpeed = arr2[parseInt(Math.random() * arr2.length)]
      })
    }, 1500)
  },
  data() {
    return {
      data: [
        {
          fileName: 'Pro.iso',
          downloadAddress: 'https://pan.baidu.com/win/10/pro.iso',
          downloadSpeed: '21.6M/S',
          completeTime: '2018-7-16 08:21:42',
          createTime: '2018-7-14 08:12:24',
          progress: 10,
          id: 1
        },
        {
          fileName: 'Pro.iso',
          downloadAddress: 'https://pan.baidu.com/win/10/pro.iso',
          downloadSpeed: '21.6M/S',
          completeTime: '2018-7-16 08:21:42',
          createTime: '2018-7-14 08:12:24',
          progress: 20,
          id: 2
        },
        {
          fileName: 'Pro.iso',
          downloadAddress: 'http://127.0.0.1:8080/#/?request=%7B%22url%22%3A%22http%3A%2F%2F192.168.2.24%2Fstatic%2Ftest.iso%22,%22heads%22%3A%7B%7D,%22body%22%3A%22%22%7D&response=%7B%22fileName%22%3A%22test.iso%22,%22totalSize%22%3A1123452928,%22supportRange%22%3Atrue%7D',
          downloadSpeed: '21.6M/S',
          completeTime: '2018-7-16 08:21:42',
          createTime: '2018-7-14 08:12:24',
          progress: 20,
          id: 3
        },
        {
          fileName: 'Pro.iso',
          downloadAddress: 'https://pan.baidu.com/win/10/pro.iso',
          downloadSpeed: '21.6M/S',
          completeTime: '2018-7-16 08:21:42',
          createTime: '2018-7-14 08:12:24',
          progress: 11,
          id: 4
        }
      ],
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
    onCheckChange({ list }) {
      console.log(list)
    },

    onSelectAll(v) {
      console.log(v)
    },
    showResolve() {
      this.resolveVisible = true
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
      }
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
