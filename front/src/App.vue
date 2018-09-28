<template>
  <div id="app">
    <i-menu mode="horizontal"
      theme="dark"
      :active-name="$route.path.substring(1)"
      @on-select="forward">
      <i-menu-item name="tasks">
        <Icon type="ios-download-outline"></Icon>
        {{ $t("nav.tasks") }}
      </i-menu-item>
      <i-menu-item name="extension">
        <Icon type="social-windows"></Icon>
        {{ $t("nav.extension") }}
      </i-menu-item>
      <i-menu-item name="setting">
        <Icon type="settings"></Icon>
        {{ $t("nav.setting") }}
      </i-menu-item>
      <i-menu-item name="about">
        <Icon type="information-circled"></Icon>
        {{ $t("nav.about") }}
      </i-menu-item>
      <i-menu-item name="support">
        <Icon type="social-usd"></Icon>
        {{ $t("nav.support") }}
      </i-menu-item>
    </i-menu>

    <Modal v-model="hasUpdate"
      :title="$t('update.checkNew')">
      <b>{{ $t('update.version') }}：</b>
      <span>{{ versionInfo.version }}</span>
      <br>
      <br>
      <b>{{ $t('update.changeLog') }}：</b>
      <div style="padding-top:10px;"
        v-html="versionInfo.description"></div>
      <span slot="footer">
        <Button @click="hasUpdate = false">{{ $t('tip.cancel') }}</Button>
        <Button type="primary"
          @click="doUpdate()">{{ $t('update.update') }}</Button>
      </span>
    </Modal>

    <Modal v-model="restatModel"
      :title="$t('update.done')">
      <h3>{{ $t('update.restart') }}</h3>
      <span slot="footer">
        <Button @click="restatModel = false">{{ $t('tip.cancel') }}</Button>
        <Button type="primary"
          @click="doRestart()">{{ $t('tip.ok') }}</Button>
      </span>
    </Modal>

    <div style="padding: 1.25rem 1.25rem">
      <keep-alive>
        <router-view/>
      </keep-alive>
    </div>

    <Spin v-if="showUpdateProgress"
      size="large"
      class="update-progress"
      fix>
      <Circle :percent="updateInfo.progress"
        :size="150">
        <h1>{{ updateInfo.progress.toFixed(2) }}%</h1>
        <p>{{ $numeral(updateInfo.speed).format('0.00b') }}/S</p>
      </Circle>
    </Spin>

  </div>
</template>

<script>
import { doUpdate, getUpdateProgress, doRestart } from './common/native.js'
export default {
  methods: {
    forward(route) {
      this.$router.push(route)
    },

    doUpdate() {
      this.showUpdateProgress = true
      this.hasUpdate = false
      //开始下载更新包
      doUpdate(this.versionInfo.path)
        .then(() => {
          //获取更新进度
          const updateProgressInterval = setInterval(() => {
            getUpdateProgress().then(result => {
              this.updateInfo.progress = result.downSize / result.totalSize * 100
              this.updateInfo.speed = result.speed
              if (result.status == 3) {
                //下载失败
                this.$Message.error({
                  content: this.$t('update.error'),
                  duration: 0
                })
              } else if (result.status == 4) {
                //下载完成
                this.restatModel = true
              }
              if (result.status == 3 || result.status == 4) {
                this.showUpdateProgress = false
                clearInterval(updateProgressInterval)
              }
            })
          }, 1000)
        })
        .catch(() => {
          this.$Message.error({
            content: this.$t('update.error'),
            duration: 0
          })
        })
    },

    doRestart() {
      doRestart().then((this.restatModel = false))
    }
  },

  data() {
    return {
      hasUpdate: false,
      showUpdateProgress: false,
      versionInfo: {},
      restatModel: false,
      updateInfo: {
        progress: 0,
        speed: 0
      }
    }
  },

  created() {
    // Check Update
    if (this.$config.needCheckUpdate) {
      this.$noSpinHttp.get(this.$config.adminServer + 'version/checkUpdate').then(result => {
        const versionInfo = result.data
        if (versionInfo && versionInfo.version > this.$config.version) {
          this.hasUpdate = true
          this.versionInfo = versionInfo
        }
      })
    }
  }
}
</script>
<style scoped>
.update-progress {
  z-index: 1001;
}
.update-progress h1 {
  color: #3f414d;
  font-size: 28px;
  font-weight: normal;
}
.update-progress p {
  color: #657180;
  font-size: 14px;
  padding-top: 10px;
}
</style>

<style>
i.action-icon {
  cursor: pointer;
  font-size: 1.25rem;
}
i.tip-icon {
  position: relative;
  top: 5px;
  padding-left: 5px;
}
i.action-icon + i.action-icon {
  padding-left: 0.625rem;
}
</style>

