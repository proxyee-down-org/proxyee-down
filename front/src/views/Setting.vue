<template>
  <Form ref="form"
    :label-width="100"
    :model="form"
    :rules="rules"
    class="setting-form">
    <Collapse :value="['down','app']">
      <Panel name="down">
        {{ $t('setting.downSetting') }}
        <div slot="content">
          <FormItem :label="$t('setting.path')"
            prop="downConfig.filePath">
            <FileChoose v-model="form.downConfig.filePath"
              style="width: 30rem" />
            <Tooltip class="item"
              placement="right">
              <Icon type="help-circled"
                class="action-icon tip-icon" />
              <div slot="content">
                <p>{{ $t('setting.pathTip') }}</p>
              </div>
            </Tooltip>
          </FormItem>
          <FormItem :label="$t('setting.connections')"
            prop="downConfig.connections">
            <Slider v-model="form.downConfig.connections"
              :min="2"
              :max="256"
              :step="2"
              show-input
              style="width: 30rem" />
            <Tooltip class="item"
              style="position:absolute;left:30rem;top:-15px;"
              placement="right">
              <Icon type="help-circled"
                class="action-icon tip-icon" />
              <div slot="content">
                <p>{{ $t('setting.connectionsTip') }}</p>
              </div>
            </Tooltip>
          </FormItem>
          <FormItem :label="$t('setting.taskLimit')"
            prop="downConfig.taskLimit">
            <InputNumber v-model="form.downConfig.taskLimit"
              :min="1"
              :max="10"></InputNumber>
          </FormItem>
          <FormItem :label="$t('setting.taskSpeedLimit')"
            prop="downConfig.speedLimit">
            <Input v-model="form.downConfig.speedLimit" />
            <span style="padding-left:5px">KB/S({{ $t('setting.speedLimitTip') }})</span>
          </FormItem>
          <FormItem :label="$t('setting.globalSpeedLimit')"
            prop="downConfig.totalSpeedLimit">
            <Input v-model="form.downConfig.totalSpeedLimit" />
            <span style="padding-left:5px">KB/S({{ $t('setting.speedLimitTip') }})</span>
          </FormItem>
        </div>
      </Panel>
      <Panel name="app">
        {{ $t('setting.appSetting') }}
        <div slot="content">
          <FormItem :label="$t('setting.language')"
            prop="appConfig.locale">
            <Select v-model="form.appConfig.locale">
              <Option value="zh-CN">中文(简体)</Option>
              <Option value="zh-TW">中文(繁體)</Option>
              <Option value="en-US">English(USA)</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('setting.uiMode')"
            prop="appConfig.uiMode">
            <Select v-model="form.appConfig.uiMode">
              <Option v-for="option in setting.uiModes"
                :key="option.value"
                :value="option.value">{{ option.text }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('setting.checkUpdate')"
            prop="appConfig.updateCheckRate">
            <Select v-model="form.appConfig.updateCheckRate">
              <Option v-for="option in setting.updateChecks"
                :key="option.value"
                :value="option.value">{{ option.text }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('setting.autoOpen')"
            prop="appConfig.autoOpen">
            <Switch v-model="form.appConfig.autoOpen"></Switch>
          </FormItem>
          <FormItem :label="$t('setting.secondProxy.secondProxy')">
            <Switch v-model="secondProxyEnable"
              @on-change="switchSecondProxy"></Switch>
            <Tooltip class="item"
              placement="right">
              <Icon type="help-circled"
                class="action-icon tip-icon" />
              <div slot="content">
                <p>{{$t('setting.secondProxy.tip')}}</p>
              </div>
            </Tooltip>
          </FormItem>
          <div v-if="secondProxyEnable">
            <FormItem :label="$t('setting.secondProxy.type')"
              prop="appConfig.proxyConfig.proxyType">
              <Select v-model="form.appConfig.proxyConfig.proxyType"
                style="width:6rem;">
                <Option value="HTTP">HTTP</Option>
                <Option value="SOCKS4">SOCKS4</Option>
                <Option value="SOCKS5">SOCKS5</Option>
              </Select>
            </FormItem>
            <FormItem :label="$t('setting.secondProxy.host')"
              prop="appConfig.proxyConfig.host">
              <Input v-model="form.appConfig.proxyConfig.host"
                class="string-input" />
            </FormItem>
            <FormItem :label="$t('setting.secondProxy.port')"
              prop="appConfig.proxyConfig.port">
              <InputNumber v-model="form.appConfig.proxyConfig.port"
                :min="1"
                :max="65535" />
            </FormItem>
            <FormItem :label="$t('setting.secondProxy.user')">
              <Input v-model="form.appConfig.proxyConfig.user"
                class="string-input" />
            </FormItem>
            <FormItem :label="$t('setting.secondProxy.pwd')">
              <Input type="password"
                v-model="form.appConfig.proxyConfig.pwd"
                class="string-input" />
            </FormItem>
          </div>
        </div>
      </Panel>
    </Collapse>
  </Form>
</template>
<script>
import FileChoose from '../components/FileChoose'
import { getConfig, setConfig } from '../common/native.js'

let debounceTimer

export default {
  name: 'setting',
  components: {
    FileChoose
  },
  data() {
    return {
      form: {
        downConfig: {},
        appConfig: {}
      },
      secondProxyEnable: false,
      rules: {
        'downConfig.speedLimit': [
          { required: true, message: this.$t('tip.notNull') },
          { pattern: /^\d+$/, message: this.$t('tip.fmtErr') }
        ],
        'downConfig.totalSpeedLimit': [
          { required: true, message: this.$t('tip.notNull') },
          { pattern: /^\d+$/, message: this.$t('tip.fmtErr') }
        ]
      }
    }
  },
  computed: {
    setting() {
      return {
        uiModes: [
          { value: 0, text: this.$t('setting.uiModeBrowser') },
          { value: 1, text: this.$t('setting.uiModeWindows') }
        ],
        updateChecks: [
          { value: 0, text: this.$t('setting.checkUpdateNever') },
          { value: 1, text: this.$t('setting.checkUpdateWeek') },
          { value: 2, text: this.$t('setting.checkUpdateStartup') }
        ]
      }
    }
  },
  watch: {
    form: {
      handler(nowVal, oldVal) {
        //不是首次加载触发
        if (Object.keys(oldVal.downConfig).length !== 0) {
          if (debounceTimer) {
            clearTimeout(debounceTimer)
          }
          debounceTimer = setTimeout(() => {
            debounceTimer = null
            this.setConfig()
          }, 300)
        }
      },
      deep: true
    }
  },
  methods: {
    switchSecondProxy(val) {
      if (val) {
        this.rules['appConfig.proxyConfig.proxyType'] = [{ required: true, message: this.$t('tip.notNull') }]
        this.rules['appConfig.proxyConfig.host'] = [{ required: true, message: this.$t('tip.notNull') }]
        this.rules['appConfig.proxyConfig.port'] = [{ required: true, message: this.$t('tip.notNull') }]
      } else if (this.form.appConfig.proxyConfig) {
        this.rules['appConfig.proxyConfig.proxyType'] = null
        this.rules['appConfig.proxyConfig.host'] = null
        this.rules['appConfig.proxyConfig.port'] = null
        this.setConfig()
      }
    },
    async getConfig() {
      let downConfig = await this.$noSpinHttp.get('http://127.0.0.1:26339/config')
      let appConfig = await getConfig()
      this.form = {
        downConfig: { ...downConfig.data },
        appConfig: { ...appConfig }
      }
      this.secondProxyEnable = !!this.form.appConfig.proxyConfig
      //设置默认值
      if (!this.form.appConfig.proxyConfig) {
        this.$set(this.form.appConfig, 'proxyConfig', {
          proxyType: 'HTTP',
          host: null,
          port: null,
          user: null,
          pwd: null
        })
      }
      if (this.form.downConfig.speedLimit > 0) {
        this.form.downConfig.speedLimit /= 1024
      }
      if (this.form.downConfig.totalSpeedLimit > 0) {
        this.form.downConfig.totalSpeedLimit /= 1024
      }
    },
    async setConfig() {
      this.$refs['form'].validate(async valid => {
        if (valid) {
          let downConfig = { ...{}, ...this.form.downConfig }
          let appConfig = { ...{}, ...this.form.appConfig }
          if (downConfig.speedLimit > 0) {
            downConfig.speedLimit *= 1024
          }
          if (downConfig.totalSpeedLimit > 0) {
            downConfig.totalSpeedLimit *= 1024
          }
          if (!this.secondProxyEnable) {
            this.$delete(appConfig, 'proxyConfig')
            this.$delete(downConfig, 'proxyConfig')
          } else {
            downConfig.proxyConfig = { ...this.form.appConfig.proxyConfig }
          }
          await this.$noSpinHttp.put('http://127.0.0.1:26339/config', downConfig)
          await setConfig(appConfig)
          this.$Message.success(this.$t('tip.saveSucc'))
          if (this.$i18n.locale != this.form.appConfig.locale) {
            this.$i18n.locale = this.form.appConfig.locale
            //强制渲染一遍，避免切换语言后下拉框不渲染的问题
            /* const uiMode = this.form.appConfig.uiMode
            const updateCheckRate = this.form.appConfig.updateCheckRate
            this.form.appConfig.uiMode = null
            this.form.appConfig.updateCheckRate = null
            setTimeout(() => {
              this.form.appConfig.uiMode = uiMode
              this.form.appConfig.updateCheckRate = updateCheckRate
            }, 0) */
          }
        }
      })
    }
  },
  created() {
    this.getConfig()
  }
}
</script>

<style scoped>
.setting-form .ivu-input-wrapper {
  width: 5rem;
}
.setting-form .ivu-select {
  width: 10rem;
}
.setting-form .string-input {
  width: 10rem;
}
</style>
