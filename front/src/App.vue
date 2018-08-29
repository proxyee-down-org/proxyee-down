<template>
  <div id="app">
    <i-menu mode="horizontal"
      theme="dark"
      :active-name="$route.path.substring(1)"
      @on-select="forward">
      <i-menu-item name="tasks">
        <Icon type="ios-download-outline"></Icon>
        {{$t("nav.tasks")}}
      </i-menu-item>
      <i-menu-item name="extension">
        <Icon type="social-windows"></Icon>
        {{$t("nav.extension")}}
      </i-menu-item>
      <i-menu-item name="setting">
        <Icon type="settings"></Icon>
        {{$t("nav.setting")}}
      </i-menu-item>
      <i-menu-item name="about">
        <Icon type="information-circled"></Icon>
        {{$t("nav.about")}}
      </i-menu-item>
      <i-menu-item name="support">
        <Icon type="social-usd"></Icon>
        {{$t("nav.support")}}
      </i-menu-item>
    </i-menu>
    <Modal v-model="hasUpdate"
      :title="$t('update.checkNew')">
      <b>{{$t('update.version')}}：</b>
      <span>{{versionInfo.version}}</span>
      <br>
      <br>
      <b>{{$t('update.changeLog')}}：</b>
      <div style="padding-top:10px;"
        v-html="versionInfo.description"></div>
      <span slot="footer">
        <Button @click="hasUpdate = false">{{$t('tip.cancel')}}</Button>
        <Button type="primary"
          @click="doUpdate()">{{$t('update.update')}}</Button>
      </span>
    </Modal>
    <Modal v-model="restatModel"
      :title="$t('update.done')">
      <h3>{{$t('update.restart')}}</h3>
      <span slot="footer">
        <Button @click="restatModel = false">{{$t('tip.cancel')}}</Button>
        <Button type="primary"
          @click="doRestart()">{{$t('tip.ok')}}</Button>
      </span>
    </Modal>
    <div style="padding: 1.25rem 1.25rem">
      <router-view/>
    </div>
  </div>
</template>

<script>
import { doUpdate, doRestart } from './common/native.js'
export default {
  methods: {
    forward(a) {
      this.$router.push(a)
    },
    doUpdate() {
      doUpdate(this.versionInfo.path)
        .then(() => {
          this.hasUpdate = false
          this.restatModel = true
        })
        .catch(() => {
          this.hasUpdate = false
          this.restatModel = true
          this.$Message.error(this.$t('update.error'))
        })
    },
    doRestart() {
      doRestart().then((this.restatModel = false))
    }
  },
  data() {
    return {
      hasUpdate: false,
      versionInfo: {},
      restatModel: false
    }
  },
  created() {
    //检查更新
    if (this.$config.needCheckUpdate) {
      this.$noSpinHttp
        .get(this.$config.adminServer + 'checkUpdate')
        .then(result => {
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

