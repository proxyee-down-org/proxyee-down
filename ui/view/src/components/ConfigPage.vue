<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
    <el-form-item label="端口" prop="proxyPort">
      <el-input v-model.number="form.proxyPort" class="num-input"></el-input>
      <el-tooltip class="item" content="http代理服务器端口号" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="分段数" prop="connections">
      <el-slider
        v-model="form.connections"
        :min="2"
        :max="256"
        :step="2"
        show-input>
      </el-slider>
      <el-tooltip class="item" content="创建新任务时默认的分段数" placement="right">
        <i class="el-icon-question" style="position: relative;top:-15px;"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="超时时间" prop="timeout">
      <el-input v-model.number="form.timeout" class="num-input" placeholder="秒"></el-input>
      <el-tooltip class="item" content="在该时间段内下载未响应则重新发起连接" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="重试次数" prop="retryCount">
      <el-input v-model.number="form.retryCount" class="num-input"></el-input>
      <el-tooltip class="item" content="分段连接失败重试次数，超过该次数则不再重新发起连接" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="启动显示" prop="autoOpen">
      <el-switch v-model="form.autoOpen"></el-switch>
      <el-tooltip class="item" content="启动成功时窗口是否显示" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="检查证书">
      <el-switch v-model="form.checkCa"></el-switch>
      <el-tooltip class="item" content="启动时是否检查证书安装情况（当证书没有正确安装时会引导安装证书）" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="自动下载" prop="autoDown">
      <el-switch v-model="form.autoDown"></el-switch>
      <el-tooltip class="item" content="嗅探到任务时是否自动开始下载" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item v-if="form.autoDown" label="下载路径" prop="autoDownPath">
      <file-choose v-model="form.autoDownPath" style="width: 80%"></file-choose>
      <el-tooltip class="item" content="自动下载时保存文件的路径" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="二级代理">
      <el-switch v-model="form.secProxyEnable"></el-switch>
      <el-tooltip class="item" content="配置下载器的代理服务器" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <div v-if="form.secProxyEnable">
      <el-form-item label="类型" prop="secProxyConfig.proxyType">
        <el-select v-model="form.secProxyConfig.proxyType" placeholder="请选择">
          <el-option
            v-for="item in proxyTypeOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </el-form-item>
      <el-form-item label="服务器" prop="secProxyConfig.host">
        <el-input v-model="form.secProxyConfig.host"></el-input>
      </el-form-item>
      <el-form-item label="端口" prop="secProxyConfig.port">
        <el-input v-model.number="form.secProxyConfig.port" class="num-input"></el-input>
      </el-form-item>
      <el-form-item label="用户名">
        <el-input v-model="form.secProxyConfig.user"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input type="password" v-model="form.secProxyConfig.pwd"></el-input>
      </el-form-item>
    </div>
    <el-form-item>
      <el-button type="primary" @click="onSubmit" :loading="load">保存</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
  import Util from '../common/util'
  import FileChoose from './base/FileChoose'

  export default {
    components: {
      FileChoose
    },
    data() {
      return {
        load: true,
        form: {
          autoOpen: true,
          autoDown: false,
          secProxyConfig: {
            proxyType: '',
            host: '',
            port: '',
            user: '',
            pwd: ''
          }
        },
        rules: {
          proxyPort: [
            {required: true, message: '不能为空'},
            {type: 'integer', min: 1, max: 65535, message: '请输入在1-65535之间的数字'},
          ],
          timeout: [
            {required: true, message: '不能为空'},
            {type: 'integer', min: 5, message: '请输入大于或等于5的数字'},
          ],
          retryCount: [
            {required: true, message: '不能为空'},
            {type: 'integer', min: 5, max: 100, message: '请输入5-100之间的数字'},
          ]
        },
        proxyTypeOptions: [{
          label: 'HTTP',
          value: 'HTTP'
        }, {
          label: 'SOCKS4',
          value: 'SOCKS4'
        }, {
          label: 'SOCKS5',
          value: 'SOCKS5'
        }]
      }
    },
    watch: {
      'form.secProxyEnable': function (val) {
        if (val) {
          this.rules['secProxyConfig.proxyType'] = [
            {required: true, message: '不能为空'}
          ];
          this.rules['secProxyConfig.host'] = [
            {required: true, message: '不能为空'}
          ];
          this.rules['secProxyConfig.port'] = [
            {required: true, message: '不能为空'},
            {type: 'integer', min: 1, max: 65535, message: '请输入在1-65535之间的数字'},
          ];
        } else {
          this.rules['secProxyConfig.proxyType'] = null;
          this.rules['secProxyConfig.host'] = null;
          this.rules['secProxyConfig.port'] = null;
        }
      },
      'form.autoDown': function (val) {
        if (val) {
          this.rules['autoDownPath'] = [
            {required: true, message: '不能为空'}
          ];
        } else {
          this.rules['autoDownPath'] = null;
        }
      }
    },
    methods: {
      onSubmit() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.load = true;
            this.$http.post('api/setConfigInfo', this.form)
            .then(() => {
              this.$message({showClose: true, message: '保存成功'});
              this.load = false;
            }).catch(() => {
              this.load = false;
            });
          }
        });
      },
    },
    created() {
      this.$http.get('api/getConfigInfo')
      .then(result => {
        if (!result.data.secProxyEnable) {
          result.data.secProxyConfig = this.form.secProxyConfig;
        }
        this.form = result.data;
        this.load = false;
      }).catch(() => {
        this.load = false;
      });
    }
  }
</script>


<style scoped>
  .el-input {
    width: 50%;
  }

  .num-input {
    width: 15%;
  }

  .el-slider {
    display: inline-block;
    padding-left: 5px;
    width: 50%;
  }
</style>
