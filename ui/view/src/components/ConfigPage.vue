<template>
  <el-form ref="form" :model="form" :rules="rules" label-width="80px" size="medium">
    <el-form-item label="端口" prop="proxyPort">
      <el-input v-model.number="form.proxyPort" class="port-input"></el-input>
    </el-form-item>
    <el-form-item label="二级代理">
      <el-switch v-model="form.secProxyEnable"></el-switch>
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
        <el-input v-model.number="form.secProxyConfig.port" class="port-input"></el-input>
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
            {type: 'number', message: '必须为数字值'}
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
            {type: 'number', message: '必须为数字值'}
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
            if(!this.form.secProxyEnable){

            }
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

  .port-input {
    width: 15%;
  }
</style>
