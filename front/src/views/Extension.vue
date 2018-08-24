<template>
  <div v-if="!certStatus">
    <Card shadow>
      <p slot="title">使用须知</p>
      <p>首次使用扩展模块时，必须安装由Proxyee Down随机生成的一个CA证书，点击下面的安装按钮并按系统的引导进行确认安装。</p>
    </Card>
    <Button type="primary"
      class="install-button"
      @click="installCert">安装</Button>
  </div>
  <div v-else>
    <div class="proxy-switch-div">
      <b>全局代理</b>
      <Switch v-model="proxySwitch"
        @on-change="changeProxyMode"></Switch>
      <Tooltip class="item"
        placement="right">
        <Icon type="help-circled"
          class="action-icon" />
        <div slot="content"
          style="white-space: normal;text-indent: 2em;">
          <p>Proxyee Down会修改系统全局的代理设置，可能会与相同机制的软件发生冲突(例如：SS、SSR)</p>
          <p>若关闭全局代理，需配合浏览器代理插件来使用(例如：SwitchyOmega)</p>
        </div>
      </Tooltip>
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
      <div>{{spinTip}}</div>
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
  toggleExtension
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
          title: '名称',
          key: 'title'
        },
        {
          title: '描述',
          key: 'description'
        },
        {
          title: '当前版本',
          key: 'currVersion',
          width: 100
        },
        {
          title: '最新版本',
          key: 'newVersion',
          width: 100
        },
        {
          title: '状态',
          key: 'meta.disabled',
          align: 'center',
          width: 100,
          render(h, params) {
            return (
              <div>
                {params.row.installed ? (
                  <Tag color="green">已安装</Tag>
                ) : (
                  <Tag>未安装</Tag>
                )}
              </div>
            )
          }
        },
        {
          title: '操作',
          key: 'action',
          align: 'center',
          width: 150,
          render(h, params) {
            return (
              <div>
                {params.row.installed ? (
                  params.row.needUpdate ? (
                    <Icon
                      type="ios-cloud-upload-outline"
                      class="action-icon"
                      title="更新"
                    />
                  ) : (
                    ''
                  )
                ) : (
                  <Icon
                    type="ios-cloud-download-outline"
                    class="action-icon"
                    title="安装"
                    nativeOnClick={() => _this.install(params.row, 0)}
                  />
                )}
                <Icon
                  type="ios-eye-outline"
                  class="action-icon"
                  title="详情"
                  nativeOnClick={() => {
                    window.open(
                      'https://github.com/proxyee-down-org/proxyee-down-extension/blob/master/' +
                        params.row.path +
                        'README.md'
                    )
                  }}
                />
              </div>
            )
          }
        },
        {
          title: '开关',
          key: 'switch',
          align: 'center',
          width: 100,
          render(h, params) {
            return (
              <Switch
                disabled={!params.row.installed}
                v-model={params.row.meta.enabled}
                onOn-change={enabled =>
                  _this.changeEnabled(enabled, params.row)
                }
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
          //安装成功
          this.loadExtensions()
        }
      })
    },
    buildPacUrl() {
      return (
        window.location.protocol +
        '//' +
        window.location.host +
        '/pac/pdown.pac'
      )
    },
    changeProxyMode(val) {
      changeProxyMode(val ? 1 : 0)
    },
    changeEnabled(enabled, row) {
      toggleExtension({ path: row.meta.path, enabled: enabled })
    },
    install(row, index) {
      let _this = this
      let extFileServers = this.$config.extFileServers
      if (index < extFileServers.length) {
        this.spinShow = true
        let extFileServer = extFileServers[index]
        let url = new URL(extFileServer)
        this.spinTip =
          '下载中...[服务器(' +
          (index + 1) +
          '/' +
          extFileServers.length +
          ')：' +
          url.host +
          ']'
        installExtension({
          server: extFileServer,
          path: row.path,
          files: row.files
        })
          .then(() => {
            this.$set(row, 'installed', true)
            this.$set(row.meta, 'path', row.path)
            this.$set(row.meta, 'enabled', true)
            this.spinShow = false
            this.$Message.success({
              content: '下载成功',
              duration: 0,
              closable: true
            })
          })
          .catch(error => {
            if (index + 1 < extFileServers.length) {
              this.$Notice.error({
                title: '下载失败',
                desc: '自动切换服务器',
                duration: 3,
                onClose() {
                  _this.install(row, index + 1)
                }
              })
            } else {
              this.$Notice.error({
                title: '下载失败',
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
      //加载代理模式
      getProxyMode().then(mode => (this.proxySwitch = mode == 1))
      //取本地已安装的插件
      getExtensions().then(localExts => {
        this.localExts = localExts
        this.searchExtensions()
      })
    },
    searchExtensions(pageSize) {
      pageSize = pageSize ? pageSize : 1
      this.$http
        .get(this.$config.extServer + 'extension/search?pageSize=' + pageSize)
        .then(result => {
          this.page = result.data
          let serverExts = this.page.data
          serverExts.forEach(serverExt => {
            serverExt.newVersion = serverExt.version
            let index = this.localExts.findIndex(
              localExt => localExt.meta.path == serverExt.path
            )
            //本地已经安装该插件
            if (index != -1) {
              let localExt = this.localExts[index]
              //判断是否需要更新
              if (serverExt.version > localExt.version) {
                serverExt.needUpdate = true
              }
              serverExt.currVersion = localExt.version
              serverExt.installed = true
              serverExt.meta = localExt.meta
            } else {
              serverExt.meta = {}
            }
          })
        })
    }
  },
  created() {
    //检查证书是否已安装
    checkCert().then(status => {
      this.certStatus = status
      //已安装
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
  float: right;
}
.proxy-switch-div {
  margin-bottom: 1.25rem;
}
.proxy-switch-div b {
  padding-right: 10px;
}
.proxy-switch-div .action-icon {
  position: relative;
  top: 5px;
  padding-left: 5px;
}
.spin-icon-load {
  animation: ani-demo-spin 1s linear infinite;
}
</style>
