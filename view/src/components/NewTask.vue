<template>
  <el-form ref="form" :model="form" label-width="80px" size="medium">
    <el-form-item label="文件名">{{form.fileName}}</el-form-item>
    <el-form-item label="文件大小">{{form.fileSize}}</el-form-item>
    <el-form-item label="支持分段">{{supportRangeText}}</el-form-item>
    <el-form-item label="分段数">
      <el-input v-model="form.connections" :disabled="!form.supportRange"/>
    </el-form-item>
    <el-form-item label="路径">
      <el-input v-model="form.path"/>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit">创建</el-button>
      <el-button>取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  export default {
    data() {
      return {
        form: {
          id: this.$route.params.id,
          fileName: '',
          fileSize: '',
          supportRange: false,
          connections: 1,
          path: 'f:/down',
        },
      }
    },
    computed: {
      supportRangeText: function () {
        // `this` 指向 vm 实例
        return this.form.supportRange ? '支持' : '不支持';
      }
    },
    methods: {
      onSubmit() {
        this.$http.post('api/startTask', this.form)
        .then((response) => {
          if (response.data == 'Y') {
            this.$router.push('/')
          }
        })
      }
    },
    created() {

      this.$http.get('api/getTask?id=' + this.form.id)
      .then((response) => {
        let res = response.data;
        this.form.fileName = res.fileName;
        this.form.fileSize = res.fileSize;
        this.form.supportRange = res.supportRange;
        this.form.connections = res.connections;
      })
    }
  }
</script>
