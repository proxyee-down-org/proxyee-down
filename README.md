![](https://i.imgur.com/dUvNgmd.jpg)  

# [Proxyee Down](https://pdown.org)
[![Author](https://img.shields.io/badge/author-monkeyWie-red.svg?style=flat-square)](https://github.com/monkeyWie)
[![Contributors](https://img.shields.io/github/contributors/proxyee-down-org/proxyee-down.svg?style=flat-square)](https://github.com/proxyee-down-org/proxyee-down/graphs/contributors)
[![Stargazers](https://img.shields.io/github/stars/proxyee-down-org/proxyee-down.svg?style=flat-square)](https://github.com/proxyee-down-org/proxyee-down/stargazers)
[![Fork](https://img.shields.io/github/forks/proxyee-down-org/proxyee-down.svg?style=flat-square)](https://github.com/proxyee-down-org/proxyee-down/fork)
[![License](https://img.shields.io/github/license/proxyee-down-org/proxyee-down.svg?style=flat-square)](https://github.com/proxyee-down-org/proxyee-down/blob/master/LICENSE)

> Proxyee Down 是一款开源的免费 HTTP 高速下载器，底层使用`netty`开发，支持自定义 HTTP 请求下载且支持扩展功能，可以通过安装扩展实现特殊的下载需求。

## 使用教程

[点击查看教程](https://github.com/proxyee-down-org/proxyee-down/wiki/%E4%BD%BF%E7%94%A8%E6%95%99%E7%A8%8B)

## 交流群

1 群**11352304**、2 群**20236964**、3 群**20233754**、4 群**737991056**

## 开发

本项目后端主要使用 `java` + `spring` + `boot` + `netty`，前端使用 `vue.js` + `iview`

### 环境
![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)

### 编译

```
git clone https://github.com/proxyee-down-org/proxyee-down.git
cd proxyee-down/front
#build html
npm install
npm run build
cd ../main
mvn clean package -Pprd
```

### 运行
```
java -jar proxyee-down-main.jar
```
