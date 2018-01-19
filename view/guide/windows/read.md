## 下载
进入[下载页面](https://github.com/monkeyWie/proxyee-down/releases)，选择proxyee-down-xx-windows-x86.zip文件下载。
## 解压并运行
将下载好的proxyee-down-xx-windows-x86.zip解压至电脑任意目录。双击proxyee-down.exe运行软件，运行成功后右下角会有提示和托盘出现。  
![1-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/1-1.png)
## 下载和安装证书
1. 打开浏览器输入**127.0.0.1:9999**，页面打开后点击**ProxyeeRoot ca.crt**下载,证书文件如下图。  

![2-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/2-1.png)  

2. 双击证书文件进行安装  

![2-2](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/2-2.png)
## 设置浏览器代理  
代理设置分为两种：非插件设置和插件设置，推荐使用插件设置。
### 非插件设置
1. 打开IE浏览器并进入设置页面：设置->Internet选项  
![3-1-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-1-1.png)  

2. 设置代理服务器：连接->局域网设置->勾选为LAN使用代理服务器->输入127.0.0.1:9999->勾选对于本地不使用代理服务器设置->确定  
![3-1-2](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-1-2.png)  

### 插件设置
下载switchyomega插件，以便快捷的切换代理服务器。[官方下载](https://www.switchyomega.com/download.html)，目前该插件只支持Chrome和Firefox浏览器，请根据自己的浏览器下载对应的版本并按照插件官网对应的指导进行安装。    

---

下面以Chrome为例配置switchyomega插件。
1. 进入插件设置界面：设置->更多工具->扩展程序->Proxy SwitchyOmega->选项  
![3-2-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-1.png)  

2. 创建代理服务器：新建情景模式->输入任意名称->勾选代理服务器选项->创建  
![3-2-2](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-2.png)  

3. 设置proxyee-down的下载代理：选择http代理协议->输入127.0.0.1:9999->应用选项  
![3-2-3](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-3.png)  

4. 切换到刚刚设置的情景模式去下载：点击switchyomega插件->选中刚刚新建的情景模式  
![3-3-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-3-1.png)  
## 完成
打开浏览器去下载吧！