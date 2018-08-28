<template>
  <div>
    <h2>如果觉得本软件不错，不妨打赏一下作者吧，让作者有动力持续更新版本和修复BUG。</h2>
    <div class="qr">
      <img src="pay/alipay.png" />
      <b>支付宝</b>
    </div>
    <div class="qr">
      <img src="pay/weipay.png" />
      <b>微信</b>
    </div>
  </div>
</template>

<script>
export default {}
</script>

<style scoped>
.qr {
  display: inline-block;
  width: 200px;
  text-align: center;
  margin: 20px;
}
.qr img {
  width: 200px;
  height: 200px;
}
.qr b {
  display: inline-block;
  padding-top: 5px;
}
</style>
