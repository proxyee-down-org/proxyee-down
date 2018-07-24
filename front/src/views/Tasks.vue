<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed" icon="plus-round" class="tasks-button">{{$t("tasks.createTasks")}}</i-button>
        <i-button type="dashed" icon="ios-play" class="tasks-button">{{$t("tasks.continueDownloading")}}</i-button>
        <i-button type="dashed" icon="ios-pause" class="tasks-button">{{$t("tasks.pauseDownloads")}}</i-button>
        <i-button type="dashed" icon="ios-trash" class="tasks-button">{{$t("tasks.deleteTask")}}</i-button>
      </div>
      <!-- <div style="min-height: 200px;">
        <i-table ref="selection" :columns="taskListsColumns" :data="taskLists" disabled-hover @on-row-click="selectTask"></i-table>
      </div> -->


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
      // taskListsColumns: [
      //   {
      //     type: "selection",
      //     align: "center",
      //     width: 60
      //   },
      //   {
      //     title: this.$t("tasks.fileName"),
      //     key: "fileName",
      //     width: 200,
      //     sortable: true
      //   },
      //   {
      //     title: this.$t("tasks.downloadAddress"),
      //     key: "downloadAddress",
      //     sortable: true
      //   },
      //   {
      //     title: this.$t("tasks.downloadSpeed"),
      //     key: "downloadSpeed",
      //     width: this.$i18n.locale === "zh" ? 110 : 140
      //   },
      //   {
      //     title: this.$t("tasks.expectedCompletionTime"),
      //     key: "completeTime",
      //     width: this.$i18n.locale === "zh" ? 150 : 210,
      //     sortable: true
      //   },
      //   {
      //     title: this.$t("tasks.createTime"),
      //     key: "createTime",
      //     width: 150,
      //     sortable: true
      //   }
      // ],
      // taskLists: [
      //   {
      //     fileName: "Windows10Pro.iso",
      //     downloadAddress: "https://pan.baidu.com/download/win/10/pro.iso",
      //     downloadSpeed: "20.1M/S",
      //     completeTime: "2018-7-16 12:20:30",
      //     createTime: "2018-7-14 12:07:56"
      //   },
      //   {
      //     fileName: "Windows8Pro.iso",
      //     downloadAddress: "https://pan.baidu.com/10/pro.iso",
      //     downloadSpeed: "11.7M/S",
      //     completeTime: "2018-7-16 11:22:31",
      //     createTime: "2018-7-16 14:07:35"
      //   },
      //   {
      //     fileName: "Pro.iso",
      //     downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
      //     downloadSpeed: "21.6M/S",
      //     completeTime: "2018-7-16 08:21:42",
      //     createTime: "2018-7-14 08:12:24"
      //   }
      // ]
    };
  },
  methods: {
    onCheckChange({ list }) {
      console.log(list);
    },

    onSelectAll(v) {
      console.log(v);
    }

    // selectTask(currentRow) {
    // console.log(currentRow);
    // },
    // taskProgress() {
    // let tasksRef = this.$refs.selection;
    // let tasksTr = tasksRef.$el.querySelectorAll("tr.ivu-table-row");

    // tasksTr.forEach(element => {
    //   // 给每个tr添加class
    //   element.className = "taskList";
    // });
    // }
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
