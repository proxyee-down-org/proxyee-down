### HTTP下载器
  自动嗅探HTTP下载请求，支持HTTP、HTTPS，多连接分块下载，突破文件服务器单连接下载速度限制，基于[proxyee](https://github.com/monkeyWie/proxyee)实现。
#### HTTPS支持
  浏览器访问http://serverIp:serverPort 进入证书下载页面，下载证书并安装(受信任的根证书颁发机构)
#### 内置插件
1. 百度云大文件、合并下载限制突破
2. 百度云合并下载特殊优化
#### 自动重试
  连接断开或超时自动重试
#### 效果预览
  ![百度云下载](https://raw.githubusercontent.com/monkeyWie/proxyee-down/dev/effect/bdy.gif)
### 运行环境
  ![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)
#### 编译
```
git clone https://github.com/monkeyWie/proxyee-down.git
cd proxyee-down/view
npm run install
npm run build
cd ..
mvn clean package
```
#### 运行
```
cd target
//指定代理服务器端口9999
java -jar  proxyee-down-1.0-SNAPSHOT.jar 9999
```