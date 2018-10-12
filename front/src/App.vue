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

    <div style="padding: 1.25rem 1.25rem">
      <keep-alive>
        <router-view />
      </keep-alive>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    forward(route) {
      this.$router.push(route)
    }
  },

  created() {
    // Check Update
    if (!this.$config.needCheckUpdate) {
      this.$noSpinHttp.get(this.$config.adminServer + 'version/checkUpdate').then(result => {
        const versionInfo = result.data
        if (versionInfo && versionInfo.version > this.$config.version) {
          this.$router.push({ path: '/about', query: { checkUpdate: true, versionInfo: JSON.stringify(versionInfo) } })
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

