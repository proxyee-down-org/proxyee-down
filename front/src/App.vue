<template>
  <div id="app">
    <i-menu mode="horizontal"
      theme="dark"
      :active-name="$route.path.substring(1)"
      @on-select="forward">
      <Badge :count="$root.badges.tasks">
        <i-menu-item name="tasks">
          <Icon type="ios-download-outline"></Icon>
          {{ $t("nav.tasks") }}
        </i-menu-item>
      </Badge>
      <Badge :count="$root.badges.extension">
        <i-menu-item name="extension">
          <Icon type="social-windows"></Icon>
          {{ $t("nav.extension") }}
        </i-menu-item>
      </Badge>
      <Badge :count="$root.badges.setting">
        <i-menu-item name="setting">
          <Icon type="settings"></Icon>
          {{ $t("nav.setting") }}
        </i-menu-item>
      </Badge>
      <Badge :count="$root.badges.about">
        <i-menu-item name="about">
          <Icon type="information-circled"></Icon>
          {{ $t("nav.about") }}
        </i-menu-item>
      </Badge>
      <Badge :count="$root.badges.support">
        <i-menu-item name="support">
          <Icon type="social-usd"></Icon>
          {{ $t("nav.support") }}
        </i-menu-item>
      </Badge>
    </i-menu>

    <div style="padding: 1.25rem 1.25rem">
      <keep-alive>
        <router-view />
      </keep-alive>
    </div>
  </div>
</template>

<script>
import { checkCert, getExtensions } from './common/native'

export default {
  methods: {
    forward(route) {
      this.$router.push(route)
    }
  },

  data() {
    return {
      badges: { tasks: 0, extension: 2, setting: 0, about: 0, support: 0 }
    }
  },

  async created() {
    // Check update
    if (this.$config.needCheckUpdate) {
      try {
        const { data: versionInfo } = await this.$noSpinHttp.get(this.$config.adminServer + 'version/checkUpdate')
        if (versionInfo && versionInfo.version > this.$config.version) {
          this.$router.push({ path: '/about', query: { checkUpdate: true, versionInfo: JSON.stringify(versionInfo) } })
        }
      } catch (e) {
        console.error(e)
      }
    }
    // Check extension update
    try {
      const status = await checkCert()
      if (status) {
        const extensions = await getExtensions()
        if (extensions.length > 0) {
          const { data: serverExtensions } = await this.$noSpinHttp.post(
            this.$config.adminServer + 'extension/checkExtensionUpdate',
            extensions.map(e => {
              return { path: e.meta.path, version: e.version }
            })
          )
          if (serverExtensions && serverExtensions.length > 0) {
            this.$root.badges.extension = serverExtensions.length
          }
        }
      }
    } catch (e) {
      console.error(e)
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

