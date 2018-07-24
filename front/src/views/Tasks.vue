<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed" icon="plus-round" class="tasks-button">{{$t("tasks.createTasks")}}</i-button>
        <i-button type="dashed" icon="ios-play" class="tasks-button">{{$t("tasks.continueDownloading")}}</i-button>
        <i-button type="dashed" icon="ios-pause" class="tasks-button">{{$t("tasks.pauseDownloads")}}</i-button>
        <i-button type="dashed" icon="ios-trash" class="tasks-button">{{$t("tasks.deleteTask")}}</i-button>
      </div>

      <tabler 
        :data="data" 
        progress
        @on-check-change="onCheckChange"
        @on-select-all="onSelectAll"
      />

    </i-content>

  </div>
</template>

<script>
import Tabler from "../components/Table";

export default {
  name: "tasks",
  components: {
    Tabler
  },
  mounted() {
    // this.taskProgress();
    let arr1 = [22, 31, 66, 71, 22, 100, 96, 56, 78, 97];
    let arr2 = ["10KB/s", "22M/s", "2.2B/s", "6G/s"];

    setInterval(() => {
      this.data.forEach(item => {
        item.progress = arr1[parseInt(Math.random() * arr1.length)];
        item.downloadSpeed = arr2[parseInt(Math.random() * arr2.length)];
      });
    }, 1500);
  },
  data() {
    return {
      data: [
        {
          fileName: "Pro.iso",
          downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
          downloadSpeed: "21.6M/S",
          completeTime: "2018-7-16 08:21:42",
          createTime: "2018-7-14 08:12:24",
          progress: 10,
          id: 1
        },
        {
          fileName: "Pro.iso",
          downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
          downloadSpeed: "21.6M/S",
          completeTime: "2018-7-16 08:21:42",
          createTime: "2018-7-14 08:12:24",
          progress: 20,
          id: 2
        },
        {
          fileName: "Pro.iso",
          downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
          downloadSpeed: "21.6M/S",
          completeTime: "2018-7-16 08:21:42",
          createTime: "2018-7-14 08:12:24",
          progress: 20,
          id: 3
        },
        {
          fileName: "Pro.iso",
          downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
          downloadSpeed: "21.6M/S",
          completeTime: "2018-7-16 08:21:42",
          createTime: "2018-7-14 08:12:24",
          progress: 11,
          id: 4
        }
      ]
    };
  },
  methods: {
    onCheckChange({ list }) {
      console.log(list);
    },

    onSelectAll(v) {
      console.log(v);
    }
  }
};
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
