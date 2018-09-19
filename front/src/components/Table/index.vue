<template>
  <section class="prye-tb">
    <div class="tb-wrapper">
      <div class="bg"></div>
      <div class="tb-head">
        <div class="tb-tr">
          <div class="ths">
            <div class="th">
              <Checkbox v-model="all"
                @on-change="setAll"></Checkbox>
            </div>
            <div class="th">{{ $t("tasks.fileName") }}</div>
            <div class="th">{{ $t("tasks.fileSize") }}</div>
            <div class="th">{{ $t("tasks.taskProgress") }}</div>
            <div class="th">{{ $t("tasks.downloadSpeed") }}</div>
            <div class="th">{{ $t("tasks.status") }}</div>
            <div class="th">{{ $t("tasks.operate") }}</div>
          </div>
        </div>
      </div>

      <div class="tb-body">
        <div class="tb-tr"
          v-for="task in taskList"
          :key="task.id">
          <div class="progress"
            :style="`width: ${ calcProgress(task) };`"></div>
          <div class="tds">
            <div class="td">
              <Checkbox v-model="checkedMap[task.id]"
                @on-change="toggleAll"></Checkbox>
            </div>
            <div class="td">{{ task.response.fileName }}</div>
            <div class="td">{{ task.response.totalSize?$numeral(task.response.totalSize).format('0.00b'):$t('tasks.unknowLeft') }}</div>
            <div class="td">{{ calcProgress(task) }}</div>
            <div class="td">{{ $numeral(task.info.speed).format('0.00b') }}/S</div>
            <div class="td">{{ calcStatus(task) }}</div>
            <div class="td">
              <Icon v-if="task.info.status === 1"
                class="action-icon"
                type="ios-pause"
                @click="$emit('on-pause', task)"></Icon>
              <Icon v-else-if="task.info.status !== 4"
                class="action-icon"
                type="ios-play"
                @click="$emit('on-resume', task)"></Icon>
              <Icon type="ios-trash"
                class="action-icon"
                @click="$emit('on-delete', task)"></Icon>
              <Icon class="action-icon"
                type="ios-folder"
                @click="$emit('on-open', task)"></Icon>
              <Poptip placement="right-end"
                :title="$t('tasks.detail')"
                width="400"
                trigger="click">
                <Icon type="ios-eye-outline"
                  style="padding-left: 0.625rem;"
                  class="action-icon"></Icon>
                <div class="file-detail"
                  slot="content">
                  <p>
                    <b>{{ $t('tasks.url') }}：</b>
                    <span>{{ task.request.url }}</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.fileName') }}：</b>
                    <span>{{ task.response.fileName }}</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.filePath') }}：</b>
                    <span>{{ task.config.filePath }}</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.fileSize') }}：</b>
                    <span>{{ $numeral(task.response.totalSize).format('0.00b') }}</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.connections') }}：</b>
                    <span>{{ task.config.connections }}</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.downloadSpeed') }}：</b>
                    <span>{{ $numeral(task.info.speed).format('0.00b') }}/S</span>
                  </p>
                  <p>
                    <b>{{ $t('tasks.status') }}：</b>
                    <span>{{ calcStatus(task) }}</span>
                  </p>
                  <p>
                    <b>{{ $t("tasks.createTime") }}：</b>
                    <span>{{ new Date(task.info.startTime).format('yyyy-MM-dd hh:mm:ss') }}</span>
                  </p>
                </div>
              </Poptip>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>
</template>

<script>
export default {
  // data
  data() {
    return {
      all: false,
      checkedMap: {}
    }
  },

  // props
  props: {
    taskList: {
      type: Array,
      required: true
    }
  },

  watch: {
    taskList() {
      if (this.taskList.length === 0) {
        this.checkedMap = {}
        this.all = false
      }
    }
  },

  // methods
  methods: {
    setAll(checked) {
      if (checked) {
        this.taskList.forEach(task => (this.checkedMap[task.id] = true))
      } else {
        for (let key in this.checkedMap) {
          this.checkedMap[key] = false
        }
      }
    },

    toggleAll(checked) {
      if (checked) {
        for (let key in this.checkedMap) {
          if (this.checkedMap[key] !== true) {
            return
          }
        }
        this.all = true
      } else {
        this.all = false
      }
    },

    calcProgress(task) {
      let progress = task.info.downSize / task.response.totalSize
      return progress ? this.$numeral(task.info.downSize / task.response.totalSize).format('0.00%') : '0%'
    },

    calcStatus(task) {
      switch (task.info.status) {
        case 0:
          return this.$t('tasks.wait')
        case 1:
          if (task.info.speed > 0 && task.response.totalSize > 0) {
            return this.$numeral((task.response.totalSize - task.info.downSize) / task.info.speed).format('00:00:00')
          } else {
            return this.$t('tasks.unknowLeft')
          }
        case 2:
          return this.$t('tasks.statusPause')
        case 3:
          return this.$t('tasks.statusFail')
        case 4:
          return this.$t('tasks.statusDone')
      }
    },

    getCheckedTasks() {
      return this.taskList.filter(task => {
        for (let key in this.checkedMap) {
          if (this.checkedMap.hasOwnProperty(key) && this.checkedMap[key] && key === task.id) {
            return true
          }
        }
        return false
      })
    }
  }
}
</script>

<style lang="less" scoped>
.prye-tb {
  .tb-wrapper {
    position: relative;
    width: 100%;
    border: 1px solid #e9eaec;
    border-bottom: 0 none;

    .bg {
      position: absolute;
      background: white;
      left: 0;
      right: 0;
      bottom: 0;
      top: 0;
      z-index: -2;
    }

    .tb-tr {
      position: relative;

      .tds,
      .ths {
        width: 100%;
        display: flex;

        .th,
        .td {
          padding: 12px 5px;
          border-bottom: 1px solid #e9eaec;
          box-sizing: border-box;
          word-break: break-all;
          display: flex;
          align-items: center;

          &:nth-child(1) {
            width: 5%;
            text-indent: 0.5em;
          }
          &:nth-child(2) {
            width: 30%;
          }
          &:nth-child(3) {
            width: 15%;
          }
          &:nth-child(4) {
            width: 15%;
          }
          &:nth-child(5) {
            width: 15%;
          }
          &:nth-child(6) {
            width: 10%;
          }
          &:nth-child(7) {
            width: 10%;
          }
        }

        .th {
          background-color: #f8f8f9;
          text-align: left;
        }
      }

      .progress {
        position: absolute;
        z-index: -1;
        height: 100%;
        background: rgba(40, 130, 214, 0.1);
        transition: all 0.6s;
      }
    }
  }
}
</style>

<style>
.file-detail p {
  padding: 2px;
}
.file-detail b {
  display: inline-block;
  width: 60px;
}
</style>

