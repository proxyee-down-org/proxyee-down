# HTTP下载器
使用本地http代理服务器方式嗅探下载请求，支持所有操作系统和大部分主流浏览器(不支持safari),支持分段下载和断点下载。
## 使用教程
### 安装
1. [Windows安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/windows/read.md)
2. [MAC安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/mac/read.md)
3. [Linux安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/linux/read.md)
### 安装成功
在安装成功之后，浏览器下载资源时会跳转到创建任务页面，然后选择保存的路径和分段数进行创建下载任务。    
![新建任务](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/new-task.png)
## 常见问题(**必看**)
在开始使用前务必看一遍[常见问题列表](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/FAQ.md)，可以解决你使用proxyee-down下载遇到的绝大多数问题。
## 常用功能
### 手动创建任务
可以根据链接来创建一个任务，支持自定义请求头和请求体，具体请[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/create/read.md)。
### 刷新任务下载链接
当任务下载链接失效了，下载没速度或失败则可以使用刷新下载链接的功能，使用新的链接继续下载，具体请[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/refresh/read.md)。
### 百度云破解
百度云大文件、合并下载限制突破,成功安装下载器后，打开百度云页面会有如下提示  
![百度云破解](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/bdy-hook.png)
### 百度云解压工具
在下载器工具页面里进入百度云解压工具，选择百度云批量下载的文件进行解压  
![百度云解压](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/bdy-unzip.png)
## 其他
### SwitchyOmega插件安装与设置
[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/switchy/read.md)
### 手动安装证书
[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/ca/read.md)
### 开放接口
[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/api/read.md)
### QQ群
1群**11352304**(已满)、2群**20236964**

# 开发
本项目依赖[proxyee](https://github.com/monkeyWie/proxyee)，因为还没上传maven中央仓库，需自行编译打包至本地仓库。
## 环境
  ![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)
## 编译
```
#proxyee依赖编译
git clone https://github.com/monkeyWie/proxyee.git
cd proxyee
mvn clean install

#proxyee-down编译
git clone https://github.com/monkeyWie/proxyee-down.git
cd proxyee-down/ui/view
npm install
npm run build
cd ../..
mvn clean package
```
## 运行
```
cd target
java -jar proxyee-down.jar
```