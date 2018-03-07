## SwitchyOmega插件安装与设置
  注：如果使用插件，最好把pd的嗅探模式设为关闭。
### 下载
[官方下载](https://www.switchyomega.com/download.html)，目前该插件只支持Chrome和Firefox内核的浏览器，请根据自己的浏览器下载对应的版本并按照官网对应的指导进行安装。
## 设置
提供两种方式进行设置  
1. 直接下载官方提供的bak文件进行导入设置(推荐)
2. 手动设置
### 导入设置
1. 下载bak文件  
[右键另存为下载](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/proxyee-down-switchy.bak)  
2. 导入bak文件  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/2-1-2.png)  
3. 切换情景模式  
- 嗅探**全站**下载请求，勾选**proxyee-down下载**  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/2-2-3.png)   
- 嗅探**百度云**下载请求，勾选**pd自动切换**  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/3-3.png) 
### 手动设置
1. 创建下载代理情景模式  
进入插件设置界面，点击新建情景模式->输入任意名称->勾选**代理服务器**->创建  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/2-2-1.png)  
2. 设置proxyee-down的下载代理：选择http代理协议->输入127.0.0.1:9999->应用选项  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/2-2-2.png)  
3. 切换到刚刚设置的情景模式  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/2-2-3.png)  

## 百度云自动切换设置
  注：此设置非必须，作用是只代理百度云的下载请求  
1. 创建下载自动切换情景模式  
进入插件设置界面，点击新建情景模式->输入任意名称->勾选**自动切换模式**->创建  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/3-1.png)  
2. 设置请求模式，添加域名并指定情景模式为之前创建的proxyee-down下载情景模式  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/3-2.png)  
  注：域名列表(pan.baidu.com,yun.baidu.com,*.baidupcs.com  )
3. 切换到刚刚设置的情景模式  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/switchy/imgs/3-3.png)  
