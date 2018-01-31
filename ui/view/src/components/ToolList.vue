<template>
  <el-row v-if="listView" type="flex" justify="center">
    <el-col :span="20">
      <el-row>
        <el-col :span="8">
          <el-tooltip class="item" content="用于百度合并下载的zip文件解压" placement="right">
            <el-button type="success" round @click="$router.push('/tools/bdyUnzip')">百度云解压
            </el-button>
          </el-tooltip>
        </el-col>
      </el-row>
    </el-col>
  </el-row>
  <div v-else>

  </div>
</template>

<script>
  export default {
    data() {
      return {
        listView: true,
      }
    }
  }
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
  .el-button {
    width: 80%;
    height: 200px;
    font-size: 24px;
  }
</style>
