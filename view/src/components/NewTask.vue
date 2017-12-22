<template>
  <el-form ref="form" :model="form" label-width="80px" size="medium">
    <el-form-item label="文件名">{{form.fileName}}</el-form-item>
    <el-form-item label="文件大小">{{totalSizeText}}</el-form-item>
    <el-form-item label="支持分段">{{supportRangeText}}</el-form-item>
    <el-form-item label="分段数">
      <el-input v-model="form.connections" :disabled="!form.supportRange"/>
    </el-form-item>
    <el-form-item label="路径">
      <file-choose v-model="form.path"></file-choose>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :disabled="load">创建</el-button>
      <el-button @click="onCancle">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import Util from '../common/util'
  import FileChoose from './FileChoose'

  export default {
    data() {
      return {
        load: true,
        form: {
          id: this.$route.params.id,
          fileName: '',
          totalSize: 0,
          supportRange: false,
          connections: 1,
          path: '',
        },
      }
    },
    components: {
      FileChoose
    },
    computed: {
      supportRangeText() {
        return this.form.supportRange ? '支持' : '不支持';
      },
      totalSizeText() {
        return Util.sizeFmt(this.form.totalSize,'未知');
      }
    },
    methods: {
      onSubmit() {
        this.$http.post('api/startTask', this.form)
        .then((response) => {
          let result = response.data;
          if (result.status == 200) {
            this.$router.push('/')
          } else {
            this.$message(result.msg);
          }
        })
      },
      onCancle() {
        window.history.go(-1);
      }
    },
    created() {
      this.$http.get('api/getTask?id=' + this.form.id)
      .then((response) => {
        let result = response.data;
        if (result.status == 200) {
          this.form.fileName = result.data.fileName;
          this.form.totalSize = result.data.totalSize;
          this.form.supportRange = result.data.supportRange;
          this.form.connections = result.data.connections;
          this.load = false;
        } else {
          this.$message(result.msg);
        }
      })
    }
  }
</script>


<style scoped>
  .el-input {
    width: 50%;
  }
</style>
