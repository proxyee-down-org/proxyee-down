<template>
  <el-form ref="form" :rules="rules" :model="form" label-width="80px" size="medium">
    <el-form-item v-if="sameTasks.length>0" label="任务列表" prop="oldId">
      <el-select v-model="form.oldId"
                 :clearable="true"
                 @change="sameTaskChange"
                 placeholder="请选择">
        <el-option
          v-for="task in sameTasks"
          :key="task.id"
          :label="task.fileName"
          :value="task.id">
          <span class="same-task-label">{{task.fileName}}</span>
          <span class="same-task-value">{{task.filePath}}</span>
        </el-option>
      </el-select>
      <el-tooltip class="item" content="刷新之前的下载任务，使用新的下载链接继续下载" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="文件名" prop="fileName">
      <el-input v-model="form.fileName" :disabled="!!form.oldId"></el-input>
    </el-form-item>
    <el-form-item label="文件大小">{{totalSizeText}}</el-form-item>
    <el-form-item label="支持分段">{{supportRangeText}}</el-form-item>
    <el-form-item label="分段数">
      <el-slider
        v-model="form.connections"
        :min="2"
        :max="256"
        :step="2"
        :disabled="!form.supportRange||!!form.oldId"
        show-input>
      </el-slider>
    </el-form-item>
    <el-form-item label="路径" prop="filePath">
      <file-choose v-model="form.filePath" :disabled="!!form.oldId"></file-choose>
    </el-form-item>
    <el-form-item label="自动解压" prop="unzip">
      <el-switch
        v-model="form.unzip">
      </el-switch>
      <el-tooltip class="item" content="当检测到任务为百度云批量下载打包的zip文件时，会在完成后会自动解压" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item v-show="form.unzip" label="解压路径" prop="unzipPath">
      <file-choose v-model="form.unzipPath"></file-choose>
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
        newTask: null,
        sameTasks: [],
        form: {
          id: this.taskId,
          oldId: '',
          fileName: '',
          totalSize: 0,
          supportRange: false,
          connections: 1,
          filePath: '',
          unzip: true,
          unzipPath: '',
        },
        rules: {
          oldId: [
            {required: true, message: '不能为空'},
          ],
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
          ],
          unzipPath: [
            {required: true, message: '不能为空'},
            {pattern: /^([a-z]:)?[/\\].*$/i, message: '格式不正确'}
          ],
        },
      }
    },
    props: [
      'taskId'
    ],
    watch: {
      'form.fileName': function (fileName) {
        this.form.unzipPath = Util.getUnzipFilePath(this.form.filePath, fileName);
      },
      'form.filePath': function (filePath) {
        this.form.unzipPath = Util.getUnzipFilePath(filePath, this.form.fileName);
      },
      'form.unzip': function (val) {
        if (val) {
          this.rules['unzipPath'] = [
            {required: true, message: '不能为空'},
            {pattern: /^([a-z]:)?[/\\].*$/i, message: '格式不正确'}
          ];
        } else {
          this.rules['unzipPath'] = null;
        }
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
        this.$confirm('确认要取消吗',
          '提示',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
          }).then(() => {
          this.$http.get('api/delNewTask?id=' + this.form.id)
          .then(() => {
            this.$emit('onCancel', arguments[0]);
          }).catch(() => {
          });
        }).catch(() => {
        });
      },
      sameTaskChange(taskId) {
        if (taskId) {
          this.sameTasks.forEach(task => {
            if (task.id == taskId) {
              Util.copy(task, this.form, ['id', 'oldId'])
            }
          })
        } else {
          Util.copy(this.newTask, this.form, ['id', 'oldId'])
        }
      },
    },
    created() {
      this.$http.get('api/getTask?id=' + this.form.id)
      .then(result => {
        if (result.data) {
          this.newTask = Util.clone(result.data.task);
          this.form = result.data.task;
          if (result.data.sameTasks && result.data.sameTasks.length > 0) {
            this.$confirm('检测到可能相同的下载任务，是否选择任务进行刷新？',
              '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
              }).then(() => {
              this.sameTasks = result.data.sameTasks;
            }).catch(() => {
            });
          }
          this.load = false;
        } else {
          this.$emit('onCancel', arguments[0]);
        }
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

  .same-task-label {
    float: left
  }

  .same-task-value {
    float: right;
    padding-left: 20px;
    color: #8492a6;
    font-size: 14px
  }
</style>
