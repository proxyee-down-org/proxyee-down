<template>
  <div class="tasks">
    <i-content :style="{padding: '0 50px'}">
      <div class="tasks-entry">
        <i-button type="dashed" icon="plus-round" class="tasks-button">创建任务</i-button>
        <i-button type="dashed" icon="ios-play" class="tasks-button">继续下载</i-button>
        <i-button type="dashed" icon="ios-pause" class="tasks-button">暂停任务</i-button>
        <i-button type="dashed" icon="ios-trash" class="tasks-button">删除任务</i-button>
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
          title: "文件名",
          key: "fileName",
          width: 200
        },
        {
          title: "下载地址",
          key: "downloadAddress"
        },
        {
          title: "下载速度",
          key: "downloadSpeed",
          width: 100
        },
        {
          title: "预计完成时间",
          key: "completeTime",
          width: 150
        },
        {
          title: "开始时间",
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
