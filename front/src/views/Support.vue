<template>
  <div>
    <Card>
      <p slot="title">如果觉得本软件不错的话，可以通过下面的二维码打赏作者，让作者有动力持续更新版本和修复BUG。</p>
      <div class="card-container">
        <img class="qr-img" src="pay/alipay.png" />
        <b>支付宝</b>
      </div>
      <div class="card-container">
        <img class="qr-img" src="pay/weipay.png" />
        <b>微信</b>
      </div>
    </Card>
    <Card v-if="softList.length>0"
      style="margin-top:20px;">
      <p slot="title">另外作者在这里推荐几款正版软件，有需要的话可以看一看，你们的每一次点击都是对作者的鼓励。</p>
      <div v-for="(soft,index) in softList"
        :key="index"
        class="card-container">
        <img class="ad-img" :src="soft.preview" />
        <a href="#"
          @click="openUrl(soft.url)"><b>{{soft.title}}</b></a>
      </div>
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
  display: inline-block;
  width: 220px;
  text-align: center;
  margin: 5px;
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
