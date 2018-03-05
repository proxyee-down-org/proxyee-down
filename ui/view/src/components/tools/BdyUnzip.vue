<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
    <el-form-item label="解压文件" prop="filePath">
      <file-choose v-model="form.filePath" model="file"></file-choose>
    </el-form-item>
    <el-form-item label="解压目录" prop="toPath">
      <file-choose v-model="form.toPath"></file-choose>
    </el-form-item>
    <el-form-item label="忽略错误" prop="ignore">
      <el-checkbox v-model="ignore"></el-checkbox>
      <el-tooltip class="item" content="忽略不是百度云批量下载zip文件的错误提示" placement="right">
        <i class="el-icon-question" style="position: relative;top:-15px;"></i>
      </el-tooltip>
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
        <div v-html="msg"></div>
      </el-form-item>
    </div>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">解压</el-button>
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
        ignore: false,
        load: false,
        unzip: false,
        taskId: '',
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
        progress: 0,
        status: null,
        msg: ''
      }
    },
    props: ['filePath', 'toPath'],
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
      },
      'form.filePath': function (filePath) {
        this.form.toPath = Util.getFileNameNoSuffix(filePath);
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
            this.$http.post('api/bdyUnzip?id=' + this.taskId+'&ignore='+this.ignore, this.form)
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
            return;
          }
          if (task.type == 'onFix') {
            return '<b>状态：</b><span>' +
              '扫描和修复中(' + Math.floor(task.fixSize * 100 / task.totalFixSize) + '%)' +
              '</span>';
          }
          if (task.type == 'onError') {
            return '<b>状态：</b><span>解压失败(' + task.errorMsg + ')</span>';
            this.load = false;
          }
          let msg = '';
          if (task.entry.dir) {
            msg = '<b>文件夹：</b><span>' + task.entry.fileName + '</span>'
          } else {
            msg = '<b>文件：</b><span>' + task.entry.fileName + '</span>';
          }
          msg = msg + '<br><b>状态：</b>';
          switch (task.type) {
            case 'onEntryStart':
            case 'onEntryWrite':
              if (task.entry.dir) {
                msg = msg + '<span>解压中(100%)</span>';
              } else {
                msg = msg + '<span>' +
                  '解压中(' + Math.floor(task.currWriteSize * 100 / task.currFileSize) + '%)' +
                  '</span>';
              }
              break;
            case 'onDone':
              msg = '<b>状态：</b>' +
                '<span>' +
                '解压完成，耗时(' + Util.timeFmt((task.endTime - task.startTime) / 1000) + ')' +
                '</span>';
              this.load = false;
              break;
          }
          return msg;
        }
        return '';
      },
    },
    mounted() {
      if (this.filePath && this.toPath) {
        this.form.filePath = this.filePath;
        this.form.toPath = this.toPath;
        this.onSubmit();
      }
    },
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
