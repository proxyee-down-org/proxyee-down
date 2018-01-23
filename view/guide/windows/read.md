## 下载
进入[下载页面](https://github.com/monkeyWie/proxyee-down/releases)，根据操作系统的位数选择对应proxyee-down-x.xx-windows-xxx.zip文件进行下载。
## 解压并运行
将下载好的proxyee-down-x.xx-windows-xxx.zip解压至电脑任意目录。双击proxyee-down.exe运行软件，运行成功后会有提示和托盘出现。  
 
![1-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/1-1.png)
## 安装证书
1. 右键点击托盘，点击安装证书

![2-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/2-1.png)  

2. 在弹出的证书安装程序中按下图步骤安装(注：证书安装的路径一定要选择受信任的根证书颁发机构)  

![2-2](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/2-2.png)
## 设置浏览器代理(**萌新不懂可跳过**)
代理设置分为两种：全局代理和局部代理，推荐使用局部代理
### 全局代理
1. 右键点击托盘，点击全局代理选择开启
![3-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-1.png)  

### 局部代理
局部代理需配合浏览器代理切换插件来使用，这里推荐SwitchyOmega，教程也以该插件为例。
1. 首先关闭全局代理，右键点击托盘，点击全局代理选择关闭
![3-2-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-1.png)  

2. 下载SwitchyOmega插件，以便快捷的切换代理服务器。[官方下载](https://www.switchyomega.com/download.html)，目前该插件只支持Chrome和Firefox浏览器，请根据自己的浏览器下载对应的版本并按照插件官网对应的指导进行安装。    

---

下面以Chrome为例配置SwitchyOmega插件。
3. 进入插件设置界面：设置->更多工具->扩展程序->Proxy SwitchyOmega->选项  
![3-2-3](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-1.png)  

4. 创建代理服务器：新建情景模式->输入任意名称->勾选代理服务器选项->创建  
![3-2-4](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-2.png)  

5. 设置proxyee-down的下载代理：选择http代理协议->输入127.0.0.1:9999->应用选项  
![3-2-5](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-3.png)  

6. 切换到刚刚设置的情景模式去下载：点击switchyomega插件->选中刚刚新建的情景模式  
![3-2-6](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-6.png)  
## 完成
打开浏览器,选择要下载的资源进行下载就会弹出下载页面了。  
![4-1](https://github.com/monkeyWie/proxyee-down/raw/master/view/guide/windows/imgs/3-2-6.png)  

