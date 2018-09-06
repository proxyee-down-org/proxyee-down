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
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/tree/v3.0#全局代理')"
          class="action-icon tip-icon" />
        <div slot="content">
          <p>{{ $t('extension.proxyTip') }}</p>
        </div>
      </Tooltip>
      <Button v-show="!proxySwitch"
        type="primary"
        @click="copyPac">{{ $t('extension.copyPac') }}</Button>
    </div>
    <Table :columns="columns"
      :data="page.data"></Table>
    <div style="margin: 10px;overflow: hidden">
      <div style="float: right;">
        <Page :total="page.totalCount"
          :current="page.pageNum"
          :page-size="page.pageSize"
          @on-change="searchExtensions(arguments[0])"></Page>
      </div>
    </div>
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
  toggleExtension,
  openUrl,
  copy
} from '../common/native.js'

export default {
  name: 'extension',
  data() {
    const _this = this
    return {
      certStatus: false,
      proxySwitch: false,
      spinShow: false,
      spinTip: '',
      columns: [
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
        {
          title: this.$t('extension.newVersion'),
          key: 'newVersion',
          width: 100
        },
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
            return (
              <div>
                {params.row.installed ? (
                  params.row.currVersion < params.row.newVersion ? (
                    <Icon
                      type="ios-cloud-upload-outline"
                      class="action-icon"
                      title={_this.$t('extension.actionUpdate')}
                      nativeOnClick={() => _this.downExtension(true, params.row, 0)}
                    />
                  ) : (
                    ''
                  )
                ) : (
                  <Icon
                    type="ios-cloud-download-outline"
                    class="action-icon"
                    title={_this.$t('extension.actionInstall')}
                    nativeOnClick={() => _this.downExtension(false, params.row, 0)}
                  />
                )}
                <Icon
                  type="ios-eye-outline"
                  class="action-icon"
                  title={_this.$t('extension.actionDetail')}
                  nativeOnClick={() => {
                    _this.openUrl(
                      'https://github.com/proxyee-down-org/proxyee-down-extension/blob/master' +
                        params.row.path +
                        '/README.md'
                    )
                  }}
                />
              </div>
            )
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
      ],
      localExts: null,
      page: {
        pageNum: 1,
        pageSize: 10,
        totalPage: 0,
        totalCount: 0,
        data: []
      }
    }
  },
  methods: {
    installCert() {
      installCert().then(status => {
        this.certStatus = status
        if (status) {
          // Install Success
          this.loadExtensions()
        }
      })
    },

    changeProxyMode(val) {
      changeProxyMode(val ? 1 : 0)
    },

    changeEnabled(enabled, row) {
      toggleExtension({ path: row.meta.path, enabled: enabled })
    },

    downExtension(isUpdate, row, index) {
      let _this = this
      let extFileServers = this.$config.extFileServers
      if (index < extFileServers.length) {
        this.spinShow = true
        let extFileServer = extFileServers[index]
        let url = new URL(extFileServer)
        this.spinTip =
          this.$t('extension.downloadingTip') +
          (index + 1) +
          '/' +
          extFileServers.length +
          ')：' +
          url.host +
          ']'
        const params = {
          server: extFileServer,
          path: row.path,
          files: row.files
        }
        let downPromise = isUpdate ? updateExtension(params) : installExtension(params)
        downPromise
          .then(() => {
            this.$set(row, 'installed', true)
            this.$set(row, 'currVersion', row.newVersion)
            this.$set(row.meta, 'path', row.path)
            this.$set(row.meta, 'enabled', true)
            this.spinShow = false
            this.$Message.success({
              content: this.$t('extension.downloadOk'),
              closable: true
            })
            // Update info to server
            this.$noSpinHttp.get(
              this.$config.adminServer +
                'extension/down?ext_id=' +
                row.id +
                '&version=' +
                row.newVersion
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
    loadExtensions() {
      // Loading agent mode
      getProxyMode().then(mode => (this.proxySwitch = mode === 1))
      // Get local installed plug-ins
      getExtensions().then(localExts => {
        this.localExts = localExts
        this.searchExtensions()
      })
    },
    searchExtensions(pageSize) {
      pageSize = pageSize ? pageSize : 1
      this.$noSpinHttp
        .get(this.$config.adminServer + 'extension/search?pageSize=' + pageSize)
        .then(result => {
          this.page = result.data
          let serverExts = this.page.data
          serverExts.forEach(serverExt => {
            serverExt.newVersion = serverExt.version
            let index = this.localExts.findIndex(localExt => localExt.meta.path == serverExt.path)
            // The plug-in has already been installed locally.
            if (index !== -1) {
              let localExt = this.localExts[index]
              serverExt.currVersion = localExt.version
              serverExt.installed = true
              serverExt.meta = localExt.meta
            } else {
              this.$set(serverExt, 'meta', { enabled: true })
            }
          })
        })
        .catch(() => {
          this.localExts.forEach(localExt => {
            localExt.installed = true
            localExt.currVersion = localExt.version
          })
          this.page = {
            pageNum: 1,
            pageSize: 10,
            totalPage: 1,
            totalCount: this.localExts.length,
            data: [...this.localExts]
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
