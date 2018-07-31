# HTTP下载器
使用本地http代理服务器方式嗅探下载请求，支持所有操作系统和大部分主流浏览器,支持分段下载和断点下载。
## 使用教程
### 下载
- [OneDrive](https://imhx-my.sharepoint.com/:f:/g/personal/pd_imhx_onmicrosoft_com/EnPrybHS3rVFuy_HdcP7RLoBwhb0k5ayJdIzwjU0hCM9-A?e=he0oIz)(推荐)
- [百度云](https://pan.baidu.com/s/1fgBnWJ0gl6ZkneGkVDIEfQ) 提取码:d92x
### 安装
1. [Windows安装教程](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/windows/read.md)
2. [MAC安装教程](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/mac/read.md)
3. [Linux安装教程](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/linux/read.md)
### 安装成功
在安装成功之后，**进入浏览器**下载资源时会跳转到创建任务页面，然后选择保存的路径和分段数进行创建下载任务。    
![新建任务](https://github.com/proxyee-down-org/proxyee-down/raw/v2.5/.guide/common/new-task.png)
## 常见问题(**必看**)
在开始使用前务必看一遍[常见问题列表](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/FAQ.md)，可以解决你使用proxyee-down下载遇到的绝大多数问题。
## 常用功能
### 手动创建任务
可以根据链接来创建一个任务，支持自定义请求头和请求体，具体请[查看](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/create/read.md)。
### 刷新任务下载链接
当任务下载链接失效了，下载没速度或失败则可以使用刷新下载链接的功能，使用新的链接继续下载，具体请[查看](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/refresh/read.md)。
### 百度云破解
#### 2018-03-29更新  
百度云近期对**批量下载**的并发连接数做了限制，如果选择的文件里包含文件夹，分段数需要建议不要超过64，否则可能会导致任务下载失败或下载速度慢。
#### 功能介绍
百度云大文件、合并下载限制突破,成功安装下载器后，打开百度云页面会有如下提示，然后在选择文件后点击下载按钮即可调用proxyee-down下载    
![百度云破解](https://github.com/proxyee-down-org/proxyee-down/raw/v2.5/.guide/common/bdy-hook.png)
### 百度云解压工具
由于百度批量下载的zip压缩包不是zip64格式，在压缩包里有超过4G文件的时候普通的解压工具并不能正确的识别文件大小从而导致压缩失败，遇到这种情况时可以在下载器左侧**工具**栏目里找到百度云解压工具进行解压
![百度云解压](https://github.com/proxyee-down-org/proxyee-down/raw/v2.5/.guide/common/bdy-unzip.png)
## 其他
### SwitchyOmega插件安装与设置
[查看](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/switchy/read.md)
### 手动安装证书
[查看](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/ca/read.md)
### 开放接口
[查看](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/api/read.md)
### QQ群
1群**11352304**、2群**20236964**、3群**20233754**、4群**737991056**
### 支持
感谢 [惶心|技术博客](https://tech.hxco.de) 提供下载服务
# 开发
导入IDE里时需要安装lombok插件。
## 环境
  ![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)
## 编译
```
#下载源码
git clone https://github.com/monkeyWie/proxyee-down.git
#前端编译
cd proxyee-down/ui/view
npm install
npm run build
#jar包编译
cd ../..
mvn clean package
```
## 运行
```
cd target
java -jar proxyee-down.jar
```
