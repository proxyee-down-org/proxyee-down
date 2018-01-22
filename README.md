# HTTP下载器
使用本地http代理服务器方式嗅探下载请求，支持所有操作系统和浏览器(IE9+),支持分段下载和断点下载。
## 内置插件
1. 百度云大文件、合并下载限制突破
2. 百度云合并下载解压工具(可解压4G大文件)
## 使用教程
### 下载
[地址](https://github.com/monkeyWie/proxyee-down/releases)  
*注：从1.6版本开始下载器仅支持64位操作系统，若是32位操作系统请下载1.5版本*。
### 安装
1. [Windows安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/view/guide/windows/read.md)
2. [MAC安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/view/guide/mac/read.md)
3. [Linux安装教程](https://github.com/monkeyWie/proxyee-down/blob/master/view/guide/linux/read.md)
### 百度云破解
在2.02+版本中，下载器配置正确之后，打开百度云页面会有如下提示
![百度云破解](https://github.com/monkeyWie/proxyee-down/raw/master/view/bdy-hook.png)
### 常见问题
1. **证书页面无法访问？**  
*先确认软件是否运行，运行还是无法访问可能是端口号被占用，打开软件设置页面修改代理端口号，保存后再访问http://127.0.0.1:修改后的端口号。*
2. **浏览器地址栏显示红色的锁，并提示证书不安全？**  
*先重启浏览器，若还是有问题，按照安装教程中证书安装步骤重新安装一遍证书。*
3. **浏览器显示代理服务器无响应？**  
*先确认软件是否运行，再检查浏览器的代理设置是否正确。*
4. **百度云下载速度太慢？**  
*调大分段数，若还是下载慢可能被百度云10kb限速了，请尝试下载文件夹或勾选多个文件一起下载。*
5. **百度云合并下载文件无法解压？**  
*可以使用下载器，工具栏的百度云解压工具进行解压。*
6. **浏览器显示该网页无法正常运行(ERR_EMPTY_RESPONSE)？**  
*进入软件设置页面检查是否设置了错误的二级代理服务器，若不需要或不了解二级代理服务器关闭即可*
7. **开始下载任务时提示服务器异常？**  
*由于windows权限问题，保存目录不能放在C盘，请修改保存目录*
8. **下载过程中是否可以关闭浏览器？**  
*可以，但是proxyee.exe软件不能关闭*  
9. **电脑重启后是否能接着下载？**  
*可以*
10. **切换了下载的代理服务器导致网页浏览过慢？**  
*下载代理服务器只是用与嗅探下载请求的，若没有新的下载任务可以不使用*
11. **百度云下载没有弹出下载页面？**  
*浏览器里按F12，看下Console里是否有请求为403，如果是403请尝试登录再下载或重新分享文件再下载*
12. **任务列表界面不显示下载速度？**  
*点击任务列表中的任务圈即可显示下载详情*

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