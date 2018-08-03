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
            <div class="th">{{ $t("tasks.downloadAddress") }}</div>
            <div class="th">{{ $t("tasks.downloadSpeed") }}</div>
            <div class="th">{{ $t("tasks.expectedCompletionTime") }}</div>
            <div class="th">{{ $t("tasks.createTime") }}</div>
            <div class="th">{{ $t("tasks.taskProgress") }}</div>
          </div>
        </div>
      </div>

      <div class="tb-body">
        <div class="tb-tr"
          v-for="item in data"
          :key="item.id">
          <div class="progress"
            :style="`width: ${ item['progress'] }%;`"
            v-if="progress"></div>
          <div class="tds">
            <div class="td">
              <Checkbox v-model="item.checked"
                @click.native="toggle($event, item)"></Checkbox>
            </div>
            <div class="td">{{ item['fileName'] }}</div>
            <div class="td">
              <a :href="item['downloadAddress']">{{ item['downloadAddress'] }}</a>
            </div>
            <div class="td">{{ item['downloadSpeed'] }}</div>
            <div class="td">{{ item['completeTime'] }}</div>
            <div class="td">{{ item['createTime'] }}</div>
            <div class="td">{{ item['progress'] + '%' }}</div>
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
      checkedList: [],
      all: false
    }
  },

  // props
  props: {
    data: {
      type: Array,
      required: true
    },
    check: {
      type: Boolean,
      default: false
    },
    progress: {
      type: Boolean,
      default: false
    }
  },

  // lifes
  mounted() {
    this.data.forEach(i => (i.checked = false))
  },

  // wa
  watch: {
    checkedList() {
      if (this.checkedList.length === this.data.length) {
        this.all = true
        return
      }
      this.all = false
    }
  },

  // methods
  methods: {
    toggle(e, item) {
      let checked = e.target.checked

      item.checked = checked

      if (checked) {
        this.checkedList.push(item)
      } else {
        this.checkedList = this.checkedList.filter(i => i !== item)
      }

      this.$emit('on-check-change', {
        list: this.checkedList,
        e
      })
    },

    setAll(v) {
      if (v) {
        this.data.forEach(i => (i.checked = true))
        this.checkedList = this.data
      } else {
        this.data.forEach(i => (i.checked = false))
        this.checkedList = []
      }

      this.all = v

      this.$emit('on-select-all', {
        list: this.checkedList,
        all: v
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
            width: 10%;
          }
          &:nth-child(3) {
            width: 50%;
          }
          &:nth-child(4) {
            width: 10%;
          }
          &:nth-child(5) {
            width: 10%;
          }
          &:nth-child(6) {
            width: 10%;
          }
          &:nth-child(7) {
            width: 5%;
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
