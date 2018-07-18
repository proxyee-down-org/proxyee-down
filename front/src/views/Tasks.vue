<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed" icon="plus-round" class="tasks-button">{{$t("tasks.createTasks")}}</i-button>
        <i-button type="dashed" icon="ios-play" class="tasks-button">{{$t("tasks.continueDownloading")}}</i-button>
        <i-button type="dashed" icon="ios-pause" class="tasks-button">{{$t("tasks.pauseDownloads")}}</i-button>
        <i-button type="dashed" icon="ios-trash" class="tasks-button">{{$t("tasks.deleteTask")}}</i-button>
      </div>
      <div style="min-height: 200px;">
        <i-table ref="selection" :columns="taskListsColumns" :data="taskLists"></i-table>
      </div>
    </i-content>
  </div>
</template>

<script>
export default {
  name: "tasks",
  data() {
    return {
      taskListsColumns: [
        {
          type: "selection",
          align: "center",
          width: 60
        },
        {
          title: this.$t("tasks.fileName"),
          key: "fileName",
          width: 200
        },
        {
          title: this.$t("tasks.downloadAddress"),
          key: "downloadAddress"
        },
        {
          title: this.$t("tasks.downloadSpeed"),
          key: "downloadSpeed",
          width: this.$i18n.locale === "zh" ? 100 : 140
        },
        {
          title: this.$t("tasks.expectedCompletionTime"),
          key: "completeTime",
          width: this.$i18n.locale === "zh" ? 100 : 200
        },
        {
          title: this.$t("tasks.createTime"),
          key: "createTime",
          width: 150
        }
      ],
      taskLists: [
        {
          fileName: "Windows10Pro.iso",
          downloadAddress: "https://pan.baidu.com/download/win/10/pro.iso",
          downloadSpeed: "20.1M/S",
          completeTime: "2018-7-16 12:20:30",
          createTime: "2018-7-14 12:07:56"
        },
        {
          fileName: "Windows8Pro.iso",
          downloadAddress: "https://pan.baidu.com/10/pro.iso",
          downloadSpeed: "11.7M/S",
          completeTime: "2018-7-16 11:22:31",
          createTime: "2018-7-16 14:07:35"
        },
        {
          fileName: "Pro.iso",
          downloadAddress: "https://pan.baidu.com/win/10/pro.iso",
          downloadSpeed: "21.6M/S",
          completeTime: "2018-7-16 08:21:42",
          createTime: "2018-7-14 08:12:24"
        }
      ]
    };
  }
};
</script>

<style lang="less">
.tasks-entry {
  margin: 20px 0;
  .tasks-button {
    margin-right: 10px;
    &:last-of-type {
      margin-right: 0;
    }
  }
}
</style>
