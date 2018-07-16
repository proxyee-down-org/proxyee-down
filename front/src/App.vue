<template>
<div id="app">
  <i-menu mode="horizontal" theme="dark" active-name="1">
    <i-menu-item name="1">
      <i-icon type="code-download"></i-icon>
      任务管理
    </i-menu-item>
    <i-submenu name="3">
      <template slot="title">
        <Icon type="settings"></Icon>
        工具列表
      </template>
      <i-menu-item name="3-1">待添加</i-menu-item>
    </i-submenu>
    <i-menu-item name="4">
      <Icon type="gear-a"></Icon>
      软件设置
    </i-menu-item>
    <i-menu-item name="4">
      <i-icon type="ios-navigate"></i-icon>
      关于项目
    </i-menu-item>
    <i-menu-item name="4">
      <i-icon type="ios-star"></i-icon>
      支持作者
    </i-menu-item>
  </i-menu>
  <router-view/>
</div>
</template>

<style lang="less">
</style>
