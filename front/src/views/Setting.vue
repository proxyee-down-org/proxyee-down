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
              <Option value="en-US">English(USA)</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('setting.uiMode')"
            prop="appConfig.uiMode">
            <Select v-model="form.appConfig.uiMode">
              <Option :value="1">{{ $t('setting.uiModeWindows') }}</Option>
              <Option :value="0">{{ $t('setting.uiModeBrowser') }}</Option>
            </Select>
          </FormItem>
          <FormItem :label="$t('setting.checkUpdate')"
            prop="appConfig.updateCheckRate">
            <Select v-model="form.appConfig.updateCheckRate">
              <Option :value="0">{{ $t('setting.checkUpdateNever') }}</Option>
              <Option :value="1">{{ $t('setting.checkUpdateWeek') }}</Option>
              <Option :value="2">{{ $t('setting.checkUpdateStartup') }}</Option>
            </Select>
          </FormItem>
        </div>
      </Panel>
    </Collapse>
    <div style="padding-top:1.25rem;">
      <Button type="primary"
        @click="setConfig">{{ $t('tip.save') }}</Button>
    </div>
  </Form>
</template>
<script>
import FileChoose from '../components/FileChoose'
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
  methods: {
    async getConfig() {
      let downConfig = await this.$http.get('http://127.0.0.1:26339/config')
      let appConfig = await this.$http.get('/native/getConfig')
      this.form = {
        downConfig: { ...downConfig.data },
        appConfig: { ...appConfig.data }
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
          let downConfig = { ...this.form.downConfig }
          if (downConfig.speedLimit > 0) {
            downConfig.speedLimit *= 1024
          }
          if (downConfig.totalSpeedLimit > 0) {
            downConfig.totalSpeedLimit *= 1024
          }
          await this.$http.put('http://127.0.0.1:26339/config', downConfig)
          await this.$http.put('/native/setConfig', this.form.appConfig)
          this.$i18n.locale = this.form.appConfig.locale
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
</style>
