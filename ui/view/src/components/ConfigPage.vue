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
        :max="128"
        :step="2"
        show-input>
      </el-slider>
      <el-tooltip class="item" content="创建新任务时默认的分段数" placement="right">
        <i class="el-icon-question"></i>
      </el-tooltip>
    </el-form-item>
    <el-form-item label="超时时间" prop="timeout">
      <el-input v-model.number="form.timeout" class="num-input" placeholder="秒"></el-input>
      <el-tooltip class="item" content="在该时间段内下载未响应则重新发起连接" placement="right">
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
    data() {
      return {
        load: true,
        form: {
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
            {type: 'integer', min: 1025, max: 65535, message: '请输入在1025-65535之间的数字'},
          ],
          timeout: [
            {required: true, message: '不能为空'},
            {type: 'integer', min: 10, message: '请输入大于或等于10的数字'},
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
            {type: 'integer', min: 1025, max: 65535, message: '请输入在1025-65535之间的数字'},
          ];
        } else {
          this.rules['secProxyConfig.proxyType'] = null;
          this.rules['secProxyConfig.host'] = null;
          this.rules['secProxyConfig.port'] = null;
        }
      }
    },
    methods: {
      onSubmit() {
        this.$refs['form'].validate((valid) => {
          if (valid) {
            this.load = true;
            this.$http.post('api/setConfigInfo', this.form)
            .then((response) => {
              let result = response.data;
              this.load = false;
              this.$message({showClose: true, message: result.msg});
            });
          }
        });
      },
    },
    created() {
      this.$http.get('api/getConfigInfo')
      .then((response) => {
        let result = response.data;
        if (result.status == 200) {
          if (!result.data.secProxyEnable) {
            result.data.secProxyConfig = this.form.secProxyConfig;
          }
          this.form = result.data;
          this.load = false;
        } else {
          this.$message({showClose: true, message: result.msg});
        }
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
    width: 70%;
  }

  .item {
    padding-left: 5px;
  }
</style>
