<template>
  <div id="app">
    <el-row type="flex" justify="center" style="height: 100%">
      <el-col :span="16" style="background-color: #F3F5F6">
        <el-container style="height: 100%">
          <el-aside width="150px">
            <el-menu
              :default-active="/^(\/[^/]*).*$/.exec($route.path)[1]"
              :router="true"
              class="el-menu-vertical-demo"
              @open="handleOpen"
              @close="handleClose"
              background-color="#545c64"
              text-color="#fff"
              active-text-color="#ffd04b"
              style="height: 100%">
              <el-menu-item index="/tasks">
                <i class="el-icon-menu"></i>
                <span slot="title">任务</span>
              </el-menu-item>
              <el-menu-item index="/tools">
                <i class="el-icon-task-tool"></i>
                <span slot="title">工具</span>
              </el-menu-item>
              <el-menu-item index="/config">
                <i class="el-icon-setting"></i>
                <span slot="title">设置</span>
              </el-menu-item>
              <el-menu-item index="/about">
                <i class="el-icon-info"></i>
                <span slot="title">关于</span>
              </el-menu-item>
              <el-menu-item index="/support">
                <i class="el-icon-task-money"></i>
                <span slot="title">打赏</span>
              </el-menu-item>
            </el-menu>
          </el-aside>
          <el-main>
            <el-row>
              <el-col :span="24">
                <div>
                  <!--<router-link to="/newTask">下载</router-link>-->
                  <router-view/>
                </div>
              </el-col>
            </el-row>
          </el-main>
        </el-container>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    name: 'app',
    methods: {
      handleOpen() {
        console.log("---handleOpen--");
      },
      handleClose() {
        console.log("---handleClose--");
      },
    }
  }
</script>

<style>
  html, body {
    height: 100%
  }

  body {
    margin: 0
  }

  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    color: #2c3e50;
    height: 100%;
  }

  @import "./assets/icon/iconfont.css";
</style>
