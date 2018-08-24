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
        <Icon type="social-yen"></Icon>
        {{$t("nav.support")}}
      </i-menu-item>
    </i-menu>
    <div style="padding: 20px 50px">
      <router-view/>
    </div>
  </div>
</template>

<script>
export default {
  methods: {
    forward(a) {
      this.$router.push(a)
    }
  }
}
</script>

<style>
i.action-icon {
  cursor: pointer;
  font-size: 1.25rem;
}
i.action-icon + i.action-icon{
  padding-left: 0.625rem;
}
</style>

