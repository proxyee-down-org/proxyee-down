<template>
  <div class="v-about">
    <Card>
      <p slot="title">{{ $t("about.project.title") }}</p>
      <ul class="project-ul">
        <li>
          <p>{{ $t("about.project.content") }}</p>
        </li>
        <li>
          <b>{{ $t("about.project.githubAddress") }}</b>
          <a href="javascript:void(0)"
            @click="openUrl('https://github.com/proxyee-down-org/proxyee-down')">
            GitHub@proxyee-down
          </a>
        </li>
        <li>
          <b>{{ $t("about.project.official") }}</b>
          <a href="javascript:void(0)"
            @click="openUrl('https://pdown.org')">
            pdown.org
          </a>
        </li>
        <li>
          <b>{{ $t("about.project.community") }}</b>
          <a href="javascript:void(0)"
            @click="openUrl('https://community.pdown.org')">
            community.pdown.org
          </a>
        </li>
        <li>
          <b>{{ $t("about.project.tutorial") }}</b>
          <a href="javascript:void(0)"
            @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/wiki/%E4%BD%BF%E7%94%A8%E6%95%99%E7%A8%8B')">
            GitHub@proxyee-down/wiki
          </a>
        </li>
        <li>
          <b>{{ $t("about.project.feedback") }}</b>
          <a href="javascript:void(0)"
            @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/issues')">
            GitHub@proxyee-down/issues
          </a>
        </li>
        <li>
          <b>{{ $t("about.project.currentVersion") }}</b>
          <span> {{ $config.version }}</span>
        </li>
        <li>
          <b>{{ $t("about.project.checkUpdate") }}</b>
          <Icon type="ios-refresh-empty"
            @click="checkUpdate()"></Icon>
        </li>
      </ul>
    </Card>

    <br>

    <Card class="team">
      <p slot="title">{{ $t("about.team.title") }}</p>
      <Card class="item">
        <div>
          <img src="team_header/monkeyWie.png"
            @click="openUrl('https://github.com/monkeyWie')">
          <b>monkeyWie</b>
        </div>
      </Card>
      <Card class="item">
        <div>
          <img src="team_header/Black-Hole.png"
            @click="openUrl('https://github.com/BlackHole1')">
          <b>Black-Hole</b>
        </div>
      </Card>
      <Card class="item">
        <div>
          <img src="team_header/NISAL.png"
            @click="openUrl('https://github.com/hiNISAL')">
          <b>NISAL</b>
        </div>
      </Card>
      <br>
    </Card>

    <Modal v-model="hasUpdate"
      :title="$t('update.checkNew')">
      <b>{{ $t('update.version') }}：</b>
      <span>{{ versionInfo.version }}</span>
      <br>
      <br>
      <b>{{ $t('update.changeLog') }}：</b>
      <div style="padding-top:10px;"
        v-html="versionInfo.description"></div>
      <span slot="footer">
        <Button @click="hasUpdate = false">{{ $t('tip.cancel') }}</Button>
        <Button type="primary"
          @click="doUpdate()">{{ $t('update.update') }}</Button>
      </span>
    </Modal>

    <Modal v-model="restatModel"
      :title="$t('update.done')">
      <h3>{{ $t('update.restart') }}</h3>
      <span slot="footer">
        <Button @click="restatModel = false">{{ $t('tip.cancel') }}</Button>
        <Button type="primary"
          @click="doRestart()">{{ $t('tip.ok') }}</Button>
      </span>
    </Modal>

    <Spin v-if="showUpdateProgress"
      size="large"
      class="update-progress"
      fix>
      <Circle :percent="updateInfo.progress"
        :size="150">
        <h1>{{ updateInfo.progress.toFixed(2) }}%</h1>
        <p>{{ $numeral(updateInfo.speed).format('0.00 ib') }}/S</p>
      </Circle>
    </Spin>

  </div>
</template>
<script>
import { openUrl, doUpdate, getUpdateProgress, doRestart } from '../common/native.js'

export default {
  name: 'about',
  data() {
    return {
      hasUpdate: false,
      showUpdateProgress: false,
      versionInfo: {},
      restatModel: false,
      updateInfo: {
        progress: 0,
        speed: 0
      }
    }
  },
  watch: {
    $route() {
      this.onRouteChange()
    }
  },
  methods: {
    openUrl(url) {
      openUrl(url)
    },
    onRouteChange() {
      if (this.$route.query.checkUpdate) {
        this.checkUpdate(this.$route.query.versionInfo)
      }
    },
    checkUpdate(versionInfoQuery) {
      if (versionInfoQuery) {
        const versionInfo = JSON.parse(versionInfoQuery)
        if (versionInfo.version > this.$config.version) {
          this.hasUpdate = true
          this.versionInfo = versionInfo
        }
      } else {
        this.$http.get(this.$config.adminServer + 'version/checkUpdate').then(result => {
          const versionInfo = result.data
          if (versionInfo && versionInfo.version > this.$config.version) {
            this.hasUpdate = true
            this.versionInfo = versionInfo
          } else {
            this.$Message.warning(this.$t('about.project.noNewVersion'))
          }
        })
      }
    },
    doUpdate() {
      this.showUpdateProgress = true
      this.hasUpdate = false
      //开始下载更新包
      doUpdate(this.versionInfo.path)
        .then(() => {
          //获取更新进度
          const updateProgressInterval = setInterval(() => {
            getUpdateProgress().then(result => {
              this.updateInfo.progress = (result.downSize / result.totalSize) * 100
              this.updateInfo.speed = result.speed
              if (result.status == 3) {
                //下载失败
                this.$Message.error({
                  content: this.$t('update.error'),
                  duration: 0
                })
              } else if (result.status == 4) {
                //下载完成
                this.restatModel = true
              }
              if (result.status == 3 || result.status == 4) {
                this.showUpdateProgress = false
                clearInterval(updateProgressInterval)
              }
            })
          }, 1000)
        })
        .catch(() => {
          this.showUpdateProgress = false
          this.$Message.error({
            content: this.$t('update.error'),
            duration: 0
          })
        })
    },

    doRestart() {
      doRestart().then((this.restatModel = false))
    }
  },
  created() {
    this.onRouteChange()
  }
}
</script>
<style lang="less" scoped>
.v-about {
  .team {
    .item {
      display: inline-table;
      margin-right: 8px;
      width: 10rem;
      height: 10rem;
      div {
        text-align: center;
        img {
          width: 6.25rem;
          border-radius: 50px;
          cursor: pointer;
        }
        b,
        span {
          display: block;
        }
        b {
          margin-bottom: 5px;
        }
      }
    }
  }
  .project-ul {
    list-style: none;
    li:not(:first-child) {
      padding-top: 5px;
    }
    .ivu-icon {
      cursor: pointer;
      font-size: 1.5em;
      position: relative;
      top: 4px;
      left: 4px;
    }
  }
}
.update-progress {
  z-index: 1001;
  h1 {
    color: #3f414d;
    font-size: 28px;
    font-weight: normal;
  }
  p {
    color: #657180;
    font-size: 14px;
    padding-top: 10px;
  }
}
</style>

