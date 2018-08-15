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
            <div class="th">{{ $t("tasks.createTime") }}</div>
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
            <div class="td">{{ $numeral(task.response.totalSize).format('0.00b') }}</div>
            <div class="td">{{ calcProgress(task) }}</div>
            <div class="td">{{ $numeral(task.info.speed).format('0.00b') }}/S</div>
            <div class="td">{{ new Date(task.info.startTime).format('yyyy-MM-dd hh:mm:ss')}}</div>
            <div class="td">{{ calcStatus(task) }}</div>
            <div class="td operate">
              <Icon v-if="task.info.status==1"
                type="ios-pause"
                @click="$emit('on-pause',task)"></Icon>
              <Icon v-else-if="task.info.status==4"
                type="ios-folder"
                @click="$emit('on-open',task)"></Icon>
              <Icon v-else
                type="ios-play"
                @click="$emit('on-resume',task)"></Icon>
              <Icon type="ios-trash"
                @click="$emit('on-delete',task)"></Icon>
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
      if (this.taskList.length == 0) {
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
      return this.$numeral(task.info.downSize / task.response.totalSize).format(
        '0.00%'
      )
    },
    calcStatus(task) {
      switch (task.info.status) {
        case 0:
          return this.$t('tasks.wait')
        case 1:
          if (task.info.speed > 0) {
            return this.$numeral(
              (task.response.totalSize - task.info.downSize) / task.info.speed
            ).format('00:00:00')
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
          if (key == task.id && this.checkedMap[key] === true) {
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
            width: 10%;
          }
          &:nth-child(4) {
            width: 10%;
          }
          &:nth-child(5) {
            width: 10%;
          }
          &:nth-child(6) {
            width: 15%;
          }
          &:nth-child(7) {
            width: 10%;
          }
          &:nth-child(8) {
            width: 10%;
          }
        }

        .th {
          background-color: #f8f8f9;
          text-align: left;
        }

        .operate i {
          cursor: pointer;
          padding-right: 0.625rem;
          font-size: 1.25rem;
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
