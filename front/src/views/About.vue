<template>
  <div>
    <h2 class="title">Proxyee Down是一款开源的免费软件，基于本软件的高速下载内核和扩展，可以方便并快速的下载所需资源。</h2>
    <ul class="list">
      <li>
        <b>项目主页：</b>
        <a href="#"
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down')">
          GitHub@proxyee-down
        </a>
      </li>
      <li>
        <b>使用教程：</b>
        <a href="#"
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/blob/master/README.md')">
          GitHub@proxyee-down/README.md
        </a>
      </li>
      <li>
        <b>BUG提交：</b>
        <a href="#"
          @click="openUrl('https://github.com/proxyee-down-org/proxyee-down/issues')">
          GitHub@proxyee-down/issues
        </a>
      </li>
      <li>
        <b>当前版本：</b>
        <span>{{$config.version}}</span>
      </li>
    </ul>
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
<style scoped>
.list {
  padding: 1.25rem;
}
.list b {
  display: inline-block;
  width: 4.5rem;
}
.list li {
  padding: 0.5rem;
}
</style>

