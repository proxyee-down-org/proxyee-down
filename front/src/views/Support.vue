<template>
  <div>
    <Card>
      <p slot="title">如果觉得本软件不错的话，可以通过下面的二维码打赏作者，让作者有动力持续更新版本和修复BUG。</p>
      <div class="card-container qr-container">
        <img class="qr-img" src="pay/alipay.png" />
        <b>支付宝</b>
      </div>
      <div class="card-container qr-container">
        <img class="qr-img" src="pay/weipay.png" />
        <b>微信</b>
      </div>
    </Card>
    <Card v-if="softList.length>0"
      style="margin-top:20px;">
      <p slot="title">另外作者在这里推荐些正版软件，有购买意向的话可以点进去看一看，你们每一次点击也是对作者的支持与鼓励。</p>
      <Card v-for="(soft,index) in softList"
        :key="index"
        class="card-container recommend-container">
        <a href="javascript:;"
          @click="openUrl(soft.url)">
          <img class="ad-img" :src="soft.preview" />
          <b>{{soft.title}}</b></a>
      </Card>
    </Card>
  </div>
</template>

<script>
import { openUrl } from '../common/native.js'
export default {
  data() {
    return {
      softList: []
    }
  },
  methods: {
    openUrl(url) {
      openUrl(url)
    }
  },
  created() {
    this.$noSpinHttp.get(this.$config.adminServer + 'recommend/soft').then(result => {
      this.softList = result.data
    })
  }
}
</script>

<style scoped>
.card-container {
  width: 220px;
  text-align: center;
  margin: 5px;
}
.qr-container {
  display: inline-block;
}
.recommend-container {
  display: inline-flex;
  height: 300px;
}
.card-container b {
  display: inline-block;
  padding-top: 5px;
}
.card-container img {
  width: 200px;
  height: 200px;
}
</style>
