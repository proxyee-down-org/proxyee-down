<template>
  <div v-if="!certStatus">
    <Card shadow>
      <p slot="title">{{ $t('extension.conditions') }}</p>
      <p>{{ $t('extension.conditionsContent') }}</p>
    </Card>
    <Button type="primary"
      class="install-button"
      @click="installCert">{{ $t('extension.install') }}</Button>
  </div>
  <div v-else>
    <div class="proxy-switch-div">
      <b>{{ $t('extension.globalProxy') }}</b>
      <Switch v-model="proxySwitch"
        @on-change="changeProxyMode"></Switch>
      <Tooltip placement="bottom">
        <Icon type="help-circled"
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/wiki/%E5%AE%89%E8%A3%85%E6%89%A9%E5%B1%95')"
          class="action-icon tip-icon" />
        <div slot="content">
          <p>{{ $t('extension.proxyTip') }}</p>
        </div>
      </Tooltip>
      <Button type="info"
        shape="circle"
        icon="loop"
        @click="loadExtensions"
        :title="$t('tip.refresh')"></Button>
      <Button type="info"
        shape="circle"
        icon="ios-copy"
        @click="copyPac"
        :title="$t('extension.copyPac')"></Button>
      <Button type="info"
        shape="circle"
        icon="android-folder-open"
        @click="installLocalExt"
        :title="$t('extension.installLocalExt')"></Button>
    </div>
    <Tabs type="card"
      :animated="false"
      v-model="activeTab">
      <TabPane :label="$t('extension.extCenter')+'('+onlinePage.totalCount+')'"
        name="online"
        icon="android-playstore">
        <Table :columns="onlineColumns"
          :data="onlinePage.data"
          :loading="onlineLoading"></Table>
        <div style="margin: 10px;overflow: hidden">
          <div style="float: right;">
            <Page :total="onlinePage.totalCount"
              :current="onlinePage.pageNum"
              :page-size="onlinePage.pageSize"
              @on-change="searchExtensions(arguments[0])"></Page>
          </div>
        </div>
      </TabPane>
      <TabPane :label="$t('extension.installStatusTrue')+'('+localAllList.length+')'"
        name="local"
        icon="social-buffer">
        <Table :columns="localColumns"
          :data="localAllList"></Table>
        <Modal v-model="settingModal"
          :title="$t('extension.setting')">
          <ExtensionSetting :settings="settings" />
          <span slot="footer">
            <Button @click="settingModal = false">{{ $t('tip.cancel') }}</Button>
            <Button type="primary"
              @click="saveSetting()">{{ $t('tip.ok') }}</Button>
          </span>
        </Modal>
      </TabPane>
    </Tabs>
    <Spin fix
      v-if="spinShow">
      <Icon type="load-c"
        class="spin-icon-load"></Icon>
      <div>{{ spinTip }}</div>
    </Spin>
  </div>
</template>
<script>
import { Icon, Tag } from 'iview'
import ExtensionSetting from '../components/ExtensionSetting.vue'
import {
  checkCert,
  installCert,
  getProxyMode,
  changeProxyMode,
  getExtensions,
  installExtension,
  updateExtension,
  installLocalExtension,
  uninstallExtension,
  toggleExtension,
  openUrl,
  copy,
  showDirChooser,
  updateExtensionSetting
} from '../common/native.js'

export default {
  name: 'extension',
  components: {
    ExtensionSetting
  },
  data() {
    return {
      certStatus: false,
      proxySwitch: false,
      activeTab: 'online',
      onlineLoading: false,
      onlinePage: {
        pageNum: 1,
        pageSize: 10,
        totalPage: 0,
        totalCount: 0,
        data: []
      },
      localAllList: [],
      spinShow: false,
      spinTip: '',
      onlineColumns: this.buildCommonColumns(),
      localColumns: this.buildCommonColumns(true),
      settingModal: false,
      settingExt: null,
      settings: []
    }
  },
  methods: {
    installCert() {
      installCert().then(status => {
        this.certStatus = status
        if (status) {
          // Install Success
          changeProxyMode(1).then(() => (this.proxySwitch = true))
          this.loadExtensions()
        }
      })
    },

    changeProxyMode(val) {
      changeProxyMode(val ? 1 : 0)
    },

    buildCommonColumns(local) {
      const _this = this
      return [
        {
          title: this.$t('extension.title'),
          key: 'title'
        },
        {
          title: this.$t('extension.description'),
          key: 'description'
        },
        {
          title: this.$t('extension.currVersion'),
          key: 'currVersion',
          width: 100
        },
        ...(local
          ? []
          : [
              {
                title: this.$t('extension.newVersion'),
                key: 'version',
                width: 100
              }
            ]),
        {
          title: this.$t('extension.installStatus'),
          key: 'meta.disabled',
          align: 'center',
          width: 100,
          render(h, params) {
            return (
              <div>
                {params.row.installed ? (
                  <Tag color="green">{_this.$t('extension.installStatusTrue')}</Tag>
                ) : (
                  <Tag>{_this.$t('extension.installStatusFalse')}</Tag>
                )}
              </div>
            )
          }
        },
        {
          title: this.$t('extension.action'),
          key: 'action',
          align: 'center',
          width: 150,
          render(h, params) {
            return [
              ...(params.row.installed
                ? [
                    ...(params.row.currVersion < params.row.version
                      ? [
                          <Icon
                            type="ios-cloud-upload-outline"
                            class="action-icon"
                            title={_this.$t('extension.actionUpdate')}
                            nativeOnClick={() => _this.downExtension(true, params.row, 0)}
                          />
                        ]
                      : []),
                    <Icon
                      type="trash-a"
                      class="action-icon"
                      title={_this.$t('extension.uninstall')}
                      nativeOnClick={() => _this.uninstallExtension(params.row)}
                    />,
                    ...(params.row.settings && params.row.settings.length
                      ? [
                          <Icon
                            type="android-settings"
                            class="action-icon"
                            title={_this.$t('extension.setting')}
                            nativeOnClick={() => {
                              _this.settingModal = true
                              _this.settingExt = params.row
                              _this.settings = params.row.settings
                              const settingValues = params.row.meta.settings
                              if (settingValues) {
                                _this.settings.forEach(s => (s.value = settingValues[s.name] || s.value))
                              }
                            }}
                          />
                        ]
                      : [])
                  ]
                : [
                    <Icon
                      type="ios-cloud-download-outline"
                      class="action-icon"
                      title={_this.$t('extension.actionInstall')}
                      nativeOnClick={() => _this.downExtension(false, params.row, 0)}
                    />
                  ]),
              <Icon
                type="ios-eye-outline"
                class="action-icon"
                title={_this.$t('extension.actionDetail')}
                nativeOnClick={() => {
                  _this.openHomepage(params.row)
                }}
              />
            ]
          }
        },
        {
          title: this.$t('extension.switch'),
          key: 'switch',
          align: 'center',
          width: 100,
          render(h, params) {
            return (
              <Switch
                disabled={!params.row.installed}
                v-model={params.row.meta.enabled}
                onOn-change={enabled => _this.changeEnabled(enabled, params.row)}
              />
            )
          }
        }
      ]
    },

    changeEnabled(enabled, row) {
      toggleExtension({ path: row.meta.path, enabled: enabled, local: row.meta.local }).then(() => {
        const localExt = this.localAllList.find(localExt => localExt.meta.path == row.meta.path)
        localExt.meta.enabled = enabled
        this.refreshExtensions()
      })
    },

    downExtension(isUpdate, row, index) {
      let _this = this
      let extFileServers = this.$config.extFileServers
      if (index < extFileServers.length) {
        this.spinShow = true
        let extFileServer = extFileServers[index]
        let url = new URL(extFileServer)
        this.spinTip =
          this.$t('extension.downloadingTip') + (index + 1) + '/' + extFileServers.length + ')：' + url.host + ']'
        const params = {
          server: extFileServer,
          path: row.meta.path,
          files: row.files
        }
        let downPromise = isUpdate ? updateExtension(params) : installExtension(params)
        downPromise
          .then(() => {
            const localExt = this.localAllList.find(localExt => localExt.meta.path == row.meta.path)
            if (localExt) {
              localExt.version = row.version
              localExt.currVersion = row.version
              localExt.title = row.title
              localExt.description = row.description
            } else {
              row.installed = true
              row.currVersion = row.version
              row.meta = { path: row.meta.path, enabled: true }
              this.localAllList.push(row)
            }
            this.getLocalExtensions(() => {
              this.refreshExtensions()
            })
            this.spinShow = false
            this.$Message.success({
              content: this.$t('extension.downloadOk'),
              closable: true
            })
            const afterBadges = this.$root.badges.extension - 1
            this.$root.badges.extension = afterBadges < 0 ? 0 : afterBadges
            // Update info to server
            this.$noSpinHttp.get(
              this.$config.adminServer +
                'extension/down?ext_id=' +
                row.id +
                '&version=' +
                row.version +
                '&pd_version=' +
                this.$config.version
            )
          })
          .catch(error => {
            if (index + 1 < extFileServers.length) {
              this.$Notice.error({
                title: this.$t('extension.downloadErr'),
                desc: this.$t('extension.downloadErrTip'),
                onClose() {
                  _this.downExtension(isUpdate, row, index + 1)
                }
              })
            } else {
              this.$Notice.error({
                title: this.$t('extension.downloadErr'),
                desc: error.response.data.error,
                duration: 0,
                closable: true
              })
              this.spinShow = false
            }
          })
      }
    },
    installLocalExt() {
      showDirChooser().then(result => {
        if (!result) {
          return
        }
        installLocalExtension(result.path)
          .then(localExt => {
            localExt.installed = true
            localExt.currVersion = localExt.version
            this.localAllList.push(localExt)
            this.refreshExtensions()
            this.$Message.success(this.$t('extension.installOk'))
            this.activeTab = 'local'
          })
          .catch(error => {
            if (error.response.status == 400) {
              this.$Message.error(this.$t('extension.installErr'))
            } else {
              this.$Message.error(this.$t('alert.error'))
            }
          })
      })
    },
    uninstallExtension(row) {
      const _this = this
      _this.$Modal.confirm({
        title: _this.$t('extension.uninstall'),
        content: _this.$t('extension.uninstallTip'),
        okText: _this.$t('tip.ok'),
        cancelText: _this.$t('tip.cancel'),
        onOk() {
          uninstallExtension(row.meta.path, row.meta.local)
            .then(() => {
              const index = _this.localAllList.findIndex(localExt => localExt.meta.path == row.meta.path)
              if (index != -1) {
                _this.localAllList.splice(index, 1)
                _this.refreshExtensions()
              }
            })
            .catch(() => _this.$Message.error(_this.$t('alert.error')))
        }
      })
    },
    getLocalExtensions(callback) {
      getExtensions().then(localAllList => {
        this.localAllList = localAllList
        this.localAllList.forEach(localExt => {
          localExt.installed = true
          localExt.currVersion = localExt.version
        })
        if (callback) {
          callback()
        }
      })
    },
    loadExtensions() {
      // Loading proxy mode
      getProxyMode().then(mode => (this.proxySwitch = mode === 1))
      // Get local installed extension
      this.getLocalExtensions(() => {
        this.searchExtensions()
      })
    },
    searchExtensions(pageSize) {
      pageSize = pageSize ? pageSize : 1
      this.onlineLoading = true
      this.$noSpinHttp
        .get(`${this.$config.adminServer}extension/search?pageSize=${pageSize}&version=${this.$config.version}`)
        .then(result => {
          this.onlinePage = result.data
          this.refreshExtensions()
        })
        .finally(() => (this.onlineLoading = false))
    },
    refreshExtensions() {
      this.onlinePage.data.forEach(onlineExt => {
        const localExt = this.localAllList.find(localExt => localExt.meta.path == onlineExt.path)
        if (localExt) {
          this.$set(onlineExt, 'installed', true)
          this.$set(onlineExt, 'currVersion', localExt.version)
          this.$set(onlineExt, 'meta', localExt.meta)
          this.$set(onlineExt, 'settings', localExt.settings)
        } else {
          onlineExt.installed = false
          onlineExt.meta = { path: onlineExt.path, enabled: false }
        }
      })
    },
    openHomepage(row) {
      const url =
        row.homepage ||
        'https://github.com/proxyee-down-org/proxyee-down-extension/blob/master' + row.meta.path + '/README.md'
      openUrl(url)
    },
    openUrl(url) {
      openUrl(url)
    },
    copyPac() {
      const { protocol, host } = window.location
      copy({
        type: 'text',
        data: `${protocol}//${host}/pac/pdown.pac?t=` + new Date().getTime()
      })
        .then(() => this.$Message.success(this.$t('tip.copySucc')))
        .catch(() => this.$Message.error(this.$t('tip.copyFail')))
    },
    saveSetting() {
      const setting = {}
      this.settingExt.settings.forEach(s => {
        setting[s.name] = s.value
      })
      updateExtensionSetting(this.settingExt.meta.path, setting)
        .then(() => this.$Message.success(this.$t('tip.saveSucc')))
        .catch(() => this.$Message.error(this.$t('tip.saveFail')))
    }
  },
  created() {
    // Check whether the certificate has been installed
    checkCert().then(status => {
      this.certStatus = status
      // Already installed
      if (status) {
        this.loadExtensions()
      }
    })
  }
}
</script>

<style scoped>
.install-button {
  margin-top: 1.25rem;
}
.proxy-switch-div {
  margin-bottom: 1.25rem;
}
.proxy-switch-div b {
  padding-right: 10px;
}
.proxy-switch-div button {
  margin-left: 25px;
}
.spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
</style>
