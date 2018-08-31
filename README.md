# HTTP下载器
Proxyee Down是一款开源的免费软件，基于本软件的高速下载内核和扩展，可以方便并快速的下载所需资源。
## 任务管理

## 扩展管理
如果要使用扩展，需要安装代理服务器生成的CA证书。

## 软件设置

## QQ群
1群**11352304**、2群**20236964**、3群**20233754**、4群**737991056**

## 开发
本项目后端主要使用java+spring boot+netty，前端使用vue.js+iview。
###环境
  ![](https://img.shields.io/badge/JAVA-1.8-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)
### 编译
注意：native打包必须使用JDK1.8版本
```
git clone https://github.com/monkeyWie/proxyee-down.git
cd proxyee-down/front
#build html
npm install
npm run build
cd ..
#build on Windows(default)
mvn clean package -P=windows -Pprd
#build on Mac
mvn clean package -P=mac -Pprd
#build on Linux
mvn clean package -P=linux -Pprd
```