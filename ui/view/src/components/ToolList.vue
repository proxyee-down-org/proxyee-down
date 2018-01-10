<template>
  <el-row type="flex" justify="center">
    <el-col :span="20">
      <el-row>
        <el-col :span="8">
          <el-button type="success" round @click="$router.push('/tools/bdyUnzip')">百度云解压</el-button>
        </el-col>
      </el-row>
    </el-col>
  </el-row>
</template>

<script>
  export default {}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
.el-button{
  width: 80%;
  height: 200px;
  font-size: 24px;
}
</style>
