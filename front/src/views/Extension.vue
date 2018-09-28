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
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/tree/v3.0#%E6%89%A9%E5%B1%95%E6%A8%A1%E5%9D%97')"
          class="action-icon tip-icon" />
        <div slot="content">
          <p>{{ $t('extension.proxyTip') }}</p>
        </div>
      </Tooltip>
      <Button type="info"
        @click="copyPac">{{ $t('extension.copyPac') }}</Button>
      <Button type="info"
        @click="installLocalExt">{{ $t('extension.installLocalExt') }}</Button>
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
  showDirChooser
} from '../common/native.js'

export default {
  name: 'extension',
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
      localColumns: this.buildCommonColumns(true)
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
                    />
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
                  _this.openUrl(
                    'https://github.com/proxyee-down-org/proxyee-down-extension/blob/master' +
                      params.row.meta.path +
                      '/README.md'
                  )
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
      toggleExtension({ path: row.meta.path, enabled: enabled }).then(() => {
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
            this.refreshExtensions()
            this.spinShow = false
            this.$Message.success({
              content: this.$t('extension.downloadOk'),
              closable: true
            })
            // Update info to server
            this.$noSpinHttp.get(
              this.$config.adminServer + 'extension/down?ext_id=' + row.id + '&version=' + row.version
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
      uninstallExtension(row.meta.path, row.meta.local)
        .then(() => {
          const index = this.localAllList.findIndex(localExt => localExt.meta.path == row.meta.path)
          if (index != -1) {
            this.localAllList.splice(index, 1)
            this.refreshExtensions()
          }
        })
        .catch(() => this.$Message.error(this.$t('alert.error')))
    },
    loadExtensions() {
      // Loading proxy mode
      getProxyMode().then(mode => (this.proxySwitch = mode === 1))
      // Get local installed extension
      getExtensions().then(localAllList => {
        this.localAllList = localAllList
        this.localAllList.forEach(localExt => {
          localExt.installed = true
          localExt.currVersion = localExt.version
        })
        this.searchExtensions()
      })
    },
    searchExtensions(pageSize) {
      pageSize = pageSize ? pageSize : 1
      this.onlineLoading = true
      this.$noSpinHttp
        .get(this.$config.adminServer + 'extension/search?pageSize=' + pageSize)
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
        } else {
          onlineExt.installed = false
          onlineExt.meta = { path: onlineExt.path, enabled: false }
        }
      })
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
