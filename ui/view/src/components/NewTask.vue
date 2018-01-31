<template>
  <el-form ref="form" :rules="rules" :model="form" label-width="80px" size="medium">
    <el-form-item label="文件名" prop="fileName">
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
    <el-form-item label="路径" prop="filePath">
      <file-choose v-model="form.filePath"></file-choose>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">创建</el-button>
      <el-button @click="onCancel">取消</el-button>
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
          id: this.taskId,
          fileName: '',
          totalSize: 0,
          supportRange: false,
          connections: 1,
          filePath: '',
        },
        rules: {
          url: [
            {required: true, message: '不能为空'},
            {type: 'url', message: '格式不正确'}
          ],
          fileName: [
            {required: true, message: '不能为空'}
          ],
          filePath: [
            {required: true, message: '不能为空'},
            {pattern: /^([a-z]:)?[/\\].*$/i, message: '格式不正确'}
          ]
        },
      }
    },
    props: [
      'taskId'
    ],
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
      onSubmit(e) {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.load = true;
            this.$http.post('api/startTask', this.form)
            .then((result) => {
              this.load = false;
              this.$emit('onSubmit', result);
            }).catch(() => {
              this.load = false;
            });
          }
        });
      },
      onCancel() {
        this.$confirm('确定要关闭吗',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
          }).then(() => {
          this.$emit('onCancel', arguments[0]);
        }).catch(() => {
        });
      }
    },
    created() {
      this.$http.get('api/getTask?id=' + this.form.id)
      .then(result => {
        this.form = result.data
        this.load = false;
      }).catch(() => {
      });
    }
  }
</script>


<style scoped>
  .el-input {
    width: 50%;
  }

  .el-checkbox + .el-checkbox {
    margin-left: 10px;
  }

  .el-slider {
    padding-left: 5px;
    width: 70%;
  }
</style>
