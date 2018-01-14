<template>
  <el-form ref="form" :model="form" label-width="80px" size="medium">
    <el-form-item label="文件名">
      <el-input v-model="form.fileName"></el-input>
    </el-form-item>
    <el-form-item label="文件大小">{{totalSizeText}}</el-form-item>
    <el-form-item label="支持分段">{{supportRangeText}}</el-form-item>
    <el-form-item label="分段数">
      <el-slider
        v-model="form.connections"
        :min="2"
        :max="128"
        :step="2"
        :disabled="!form.supportRange"
        show-input>
      </el-slider>
    </el-form-item>
    <el-form-item label="路径">
      <file-choose v-model="form.filePath"></file-choose>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">创建</el-button>
      <el-button @click="onCancle">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import Util from '../common/util'
  import FileChoose from './base/FileChoose'

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
          filePath: '',
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
        return Util.sizeFmt(this.form.totalSize, '未知');
      }
    },
    methods: {
      onSubmit() {
        this.load = true;
        this.$http.post('api/startTask', this.form)
        .then((response) => {
          let result = response.data;
          if (result.status == 200) {
            this.$router.push('/')
          } else {
            this.load = false;
            this.$message({showClose: true,message:result.msg});
          }
        });
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
          this.form = result.data
          this.load = false;
        } else {
          this.$message({showClose: true,message:result.msg});
        }
      })
    }
  }
</script>


<style scoped>
  .el-input {
    width: 50%;
  }

  .el-slider {
    padding-left: 5px;
    width: 70%;
  }
</style>
