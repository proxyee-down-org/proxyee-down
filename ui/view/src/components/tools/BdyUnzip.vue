<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
    <el-form-item label="解压文件" prop="filePath">
      <file-choose v-model="form.filePath" model="file"></file-choose>
    </el-form-item>
    <el-form-item label="解压目录" prop="toPath">
      <file-choose v-model="form.toPath"></file-choose>
    </el-form-item>
    <div v-if="unzip">
      <el-form-item prop="toPath">
        <el-progress
          :text-inside="true"
          :stroke-width="18"
          :percentage="progress"
          :status="status"></el-progress>
      </el-form-item>
      <el-form-item prop="toPath">
        <span>{{msg}}</span>
      </el-form-item>
    </div>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">确定</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import FileChoose from '../base/FileChoose'
  import Util from '../../common/util'
  import {mapState} from 'vuex'

  export default {
    data() {
      return {
        load: false,
        unzip: false,
        taskId: '',
        form: {
          filePath: 'F:\\down\\test2测试.zip',
          toPath: 'F:\\down',
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
        progress: 0,
        status: null,
        msg: ''
      }
    },
    props:['test'],
    computed: {
      ...mapState('unzips', [
          'unzipTasks',
        ],
      ),

    },
    watch: {
      unzipTasks(unzipTasks) {
        if (this.taskId && unzipTasks) {
          unzipTasks.forEach(t => {
            if (t.id == this.taskId) {
              this.progress = this.progressHandle(t);
              this.status = this.statusHandle(t);
              this.msg = this.msgHandle(t);
              return false;
            }
          });
        }
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
            this.taskId = Util.uuid();
            this.$http.post('api/bdyUnzip?id='+this.taskId, this.form)
            .then(result => {
            }).catch(() => {
              this.load = false
            })
          }
        });
      },
      progressHandle(task) {
        if (task) {
          let writeSize = task.totalWriteSize;
          let totalSize = task.totalFileSize;
          if (writeSize > 0 && totalSize > 0) {
            return Math.floor(writeSize * 100 / totalSize);
          }
        }
        return 0;
      },
      statusHandle(task) {
        if (task) {
          if (task.type == 'onDone') {
            this.progress = 100;
            return 'success';
          } else if (task.type == 'onError') {
            return 'exception';
          }
        }
        return null;
      },
      msgHandle(task) {
        if (task) {
          if (task.type == 'onStart') {
            this.unzip = true;
            return '';
          }
          let msg = '';
          if (task.entry.dir) {
            msg = '文件夹：' + task.entry.fileName
          } else {
            msg = '文件：' + task.entry.fileName;
          }
          switch (task.type) {
            case 'onErrorSize':
              msg = msg + ' 修复中';
              break;
            case 'onFixedSize':
              msg = msg + ' 修复成功';
              break;
            case 'onEntryBegin':
              msg = msg + ' 开始解压';
              break;
            case 'onEntryWrite':
              if (task.entry.dir) {
                msg = msg + ' 解压中';
              } else {
                msg = msg + ' 解压中：(' + task.currWriteSize + '/' + task.currFileSize + ')';
              }
              break;
            case 'onEntryEnd':
              if (task.entry.dir) {
                msg = msg + ' 解压完成';
              } else {
                msg = msg + ' 解压完成：(' + task.currWriteSize + '/' + task.currFileSize + ')';
              }
              break;
            case 'onDone':
              msg = '解压完成，耗时：' + Util.timeFmt((task.endTime - task.startTime) / 1000);
              this.load = false;
              break;
            case 'onError':
              msg = '解压失败';
              this.load = false;
              break;
          }
          return msg;
        }
        return '';
      },
    }
  }
</script>

<style scoped>
  .el-input {
    width: 50%;
  }

  .el-progress {
    width: 50%;
  }
</style>
