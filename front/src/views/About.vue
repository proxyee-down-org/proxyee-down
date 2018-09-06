<template>
  <div class="v-about">
    <Card>
      <p slot="title">{{ $t("about.project.title") }}</p>
      <p>{{ $t("about.project.content") }}</p>
      <br>

      <span>{{ $t("about.project.githubAddress") }}</span>
      <a href="javascript:void(0)" @click="openUrl('https://github.com/proxyee-down-org/proxyee-down')">
        GitHub@proxyee-down
      </a>
      <br>

      <span>{{ $t("about.project.tutorial") }}</span>
      <a href="javascript:void(0)"
        @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/blob/master/README.md')">
        GitHub@proxyee-down/README.md
      </a>
      <br>

      <span>{{ $t("about.project.feedback") }}</span>
      <a href="javascript:void(0)"
        @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/issues')">
        GitHub@proxyee-down/issues
      </a>
      <br>

      <span>{{ $t("about.project.currentVersion") }}</span>
      <span> {{ $config.version }}</span>
    </Card>
    
    <br>

    <Card class="team">
      <p slot="title">{{ $t("about.team.title") }}</p>
      <Card class="item">
        <div>
          <img src="team_header/monkeyWie.png" @click="openUrl('https://github.com/monkeyWie')">
          <b>monkeyWie</b>
        </div>
      </Card>
      <Card class="item">
        <div>
          <img src="team_header/Black-Hole.png" @click="openUrl('https://github.com/BlackHole1')">
          <b>Black-Hole</b>
        </div>
      </Card>
      <Card class="item">
        <div>
          <img src="team_header/NISAL.png" @click="openUrl('https://github.com/hiNISAL')">
          <b>NISAL</b>
        </div>
      </Card>
      <br>
    </Card>
  </div>
</template>
<script>
import { openUrl } from '../common/native.js'

export default {
  name: 'about',
  methods: {
    openUrl(url) {
      openUrl(url)
    }
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
          b, span {
            display: block;
          }
          b{
            margin-bottom: 5px;
          }
        }
      } 
    }
  }
</style>

