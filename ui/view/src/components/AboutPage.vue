<template>
  <div style="padding: 20px 50px">
    <h1 class="title">关于proxyee-down</h1>
    <el-row>
      <el-col :span="3">
        <span>项目主页</span>
      </el-col>
      <el-col :span="16">
        <span>
          <a target="_blank"
             href="https://github.com/monkeyWie/proxyee-down">GitHub@proxyee-down</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <span>使用教程</span>
      </el-col>
      <el-col :span="16">
        <span>
          <a target="_blank"
             href="https://www.zhanghuanglong.com/detail/another-download-of-magic-proxyee-down">文章@°只为大大</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <span>软件下载</span>
      </el-col>
      <el-col :span="16">
        <span>
          <a target="_blank"
             href="https://github.com/monkeyWie/proxyee-down/releases">GitHub@proxyee-down/releases</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <span>BUG提交</span>
      </el-col>
      <el-col :span="16">
        <span>
          <a target="_blank" href="https://github.com/monkeyWie/proxyee-down/issues">GitHub@proxyee-down/issue</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="3">
        <span>QQ群</span>
      </el-col>
      <el-col :span="16">
        <span>11352304</span>
      </el-col>
    </el-row>
    <h1 class="title">软件升级</h1>
    <el-row>
      <el-col :span="3">
        <span>当前版本</span>
      </el-col>
      <el-col :span="16">
        <span>2.0</span>
      </el-col>
    </el-row>
    <h1 class="title">感谢以下开源项目</h1>
    <el-row>
      <el-col :span="16">
        <span>
          <a target="_blank" href="http://spring.io/">spring</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <span>
          <a target="_blank" href="http://netty.io/">netty</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <span>
          <a target="_blank" href="https://cn.vuejs.org/">vue.js</a>
        </span>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="16">
        <span>
          <a target="_blank" href="http://element.eleme.io">Element UI</a>
        </span>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  export default {
    data() {
      return {
        version: null
      }
    },
    created() {
      this.$http.get('api/getConfigInfo')
      .then((response) => {
        let result = response.data;
        if (result.status == 200) {
          if (!result.data.secProxyEnable) {
            result.data.secProxyConfig = this.form.secProxyConfig;
          }
          this.form = result.data;
          this.load = false;
        } else {
          this.$message({showClose: true, message: result.msg});
        }
      });
    }
  }
</script>

<style scoped>
  .el-row {
    padding-top: 10px;
  }

  .title {
    font-size: 28px;
  }

  span {
    font-size: 18px;
  }
</style>
