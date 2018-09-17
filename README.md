# HTTP 下载器

Proxyee Down 是一款开源的免费 HTTP 高速下载器，底层使用`netty`开发，支持自定义 HTTP 请求下载且支持扩展功能，可以通过安装扩展实现特殊的下载需求。

## 软件下载

- [OneDrive下载](https://imhx-my.sharepoint.com/:f:/g/personal/pd_imhx_onmicrosoft_com/EnPrybHS3rVFuy_HdcP7RLoBwhb0k5ayJdIzwjU0hCM9-A?e=he0oIz)(推荐)  
  感谢 [惶心|技术博客](https://tech.hxco.de) 提供
- 官网下载：  
  官网带宽比较低，建议用OneDrive下载  
  - [windows](http://api.pdown.org/download/release?os=windows)
  - [mac](http://api.pdown.org/download/release?os=mac)
  - [linux](https://github.com/proxyee-down-org/proxyee-down/releases)

## 使用说明

- **windows**:  
  下载好 windows 版本的压缩包之后，解压至任意目录，会得到一个文件夹，执行文件夹里面的`Proxyee Down.exe`文件即可。  
  (_注：360 可能会报毒，需要加入白名单_)
  ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-13-49-38.png)
- **mac**:  
  下载好 mac 版本的压缩包之后，解压至任意目录，会得到一个`Proxyee Down`App，双击运行即可。  
  (_注：mac 系统切换代理和安装证书需要管理员权限，所以在启动时会提示输入密码_)
  ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-13-51-38.png)
- **linux**:  
  linux 系统目前没有打原生包，要自行下载 jar 包运行，需安装 JRE 或 JDK(_要求版本不低于 1.8_)，下载完成后在命令行中运行：
  ```
  java -jar proxyee-down-main.jar
  ```
  (_注：如果使用 openjdk 的话需要安装 openjfx_)

## 任务模块

用于管理下载任务，可以在此页面创建、查看、删除、暂停、恢复下载任务。

- **进阶**
  - [自定义下载请求](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/create/read.md)
  - [刷新任务下载链接](https://github.com/proxyee-down-org/proxyee-down/blob/v2.5/.guide/common/refresh/read.md)

## 扩展模块

在开启扩展模块时一定要手动安装一个由 Proxyee Down 随机生成的一个 CA 证书用于`HTTPS MITM`的支持。

- **安装证书**

  进入扩展页面，如果软件检测到没有安装 Proxyee Down CA 证书时，会有对应的安装提示，接受的话点击安装按照系统指引即可安装完毕。
  ![安装证书](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-08-36.png)

- **扩展商店**

  安装完证书后会进入扩展商店页面，目前扩展商店只有一款百度云下载扩展，以后会陆续开发更多的扩展(_例如：各大网站的视频下载扩展、其他网盘的下载扩展等等_)。
  ![扩展商城](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-12-21.png)

- **扩展安装**

  在操作栏找到安装按钮，点击安装即可安装扩展。
  ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-26-44.png)

- **全局代理**

  全局代理默认是不开启的，开启 Proxyee Down 会根据启用的扩展进行对应的系统代理设置，可能会与相同机制的软件发生冲突(_例如：SS、SSR_)。
  如果不使用全局代理，可以点击`复制PAC链接`，配合[SwitchyOmega 插件](https://www.switchyomega.com/)来使用。

- **其他相关**

  - **SwitchyOmega 设置教程**  
    1. 新建情景模式，选择 PAC 情景模式类型。
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-25-34.png)
    2. 把复制的 PAC 链接粘贴进来并点击立即更新情景模式然后保存。
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-30-30.png)
    3. 切换情景模式进行下载  
      ![](https://monkeywie.github.io/2018/09/05/proxyee-down-3-0-guide/2018-09-05-14-32-00.png)

  - **参与扩展开发**  
    详见[proxyee-down-extension](https://github.com/proxyee-down-org/proxyee-down-extension)

  - **扩展实现原理**  
    扩展功能是由 MITM(中间人攻击)技术实现的，使用[proxyee](https://github.com/monkeyWie/proxyee)框架拦截和修改`HTTP`、`HTTPS`的请求和响应报文，从而实现对应的扩展脚本注入。

## QQ 群

1 群**11352304**、2 群**20236964**、3 群**20233754**、4 群**737991056**

## 开发

本项目后端主要使用 java+spring boot+netty，前端使用 vue.js+iview。

### 环境
![](https://img.shields.io/badge/JAVA-1.8%2B-brightgreen.svg) ![](https://img.shields.io/badge/maven-3.0%2B-brightgreen.svg) ![](https://img.shields.io/badge/node.js-8.0%2B-brightgreen.svg)

### 编译

```
git clone https://github.com/monkeyWie/proxyee-down.git
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
