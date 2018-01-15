<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
    <el-form-item label="解压文件" prop="filePath">
      <file-choose v-model="form.filePath" model="file"></file-choose>
    </el-form-item>
    <el-form-item label="解压目录" prop="toPath">
      <file-choose v-model="form.toPath"></file-choose>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">确定</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import FileChoose from '../base/FileChoose'

  export default {
    data() {
      return {
        load: false,
        form: {
          filePath: '',
          toPath: '',
        },
        rules: {
          filePath: [
            {required: true, message: '不能为空'},
            {pattern: /^([a-z]:)?[/\\].*$/i, message: '格式不正确'}
          ],
          toPath: [
            {required: true, message: '不能为空'},
            {pattern: /^([a-z]:)?[/\\].*$/i, message: '格式不正确'}
          ]
        },
      }
    },
    components: {
      FileChoose
    },
    methods: {
      onSubmit() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.load = true;
            this.$http.post('api/bdyUnzip', this.form)
            .then((response) => {
              this.load = false;
              let result = response.data;
              if (result.status == 200) {
                this.$message({showClose: true, message: "解压完成", duration: 0});
              } else {
                this.$message({showClose: true, message: result.msg, duration: 0});
              }
            })
          }
        });
      }
    },
  }
</script>

<style scoped>
  .el-input {
    width: 50%;
  }
</style>
