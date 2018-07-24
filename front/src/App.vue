<template>
<div id="app">
  <i-menu mode="horizontal" theme="dark" active-name="1">
    <i-menu-item name="1">
      <i-icon type="code-download"></i-icon>
      {{$t("nav.taskManager")}}
    </i-menu-item>
    <i-submenu name="3">
      <template slot="title">
        <i-icon type="settings"></i-icon>
        {{$t("nav.toolList")}}
      </template>
      <i-menu-item name="3-1">待添加</i-menu-item>
    </i-submenu>
    <i-menu-item name="4">
      <i-icon type="gear-a"></i-icon>
      {{$t("nav.softwareSetting")}}
    </i-menu-item>
    <i-menu-item name="4">
      <i-icon type="ios-navigate"></i-icon>
      {{$t("nav.aboutProject")}}
    </i-menu-item>
    <i-menu-item name="4">
      <i-icon type="ios-star"></i-icon>
      {{$t("nav.supportUs")}}
    </i-menu-item>
  </i-menu>
  <router-view/>
</div>
</template>

<style lang="less">
</style>
