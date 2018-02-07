## 准备

1. [下载安装jre](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)
2. [下载proxyee-down-*.*-jar.zip](https://github.com/monkeyWie/proxyee-down/releases)
3. [下载安装SwitchyOmega](https://github.com/FelisCatus/SwitchyOmega)(注意仅支持chrome浏览器)

## 安装配置

1. 解压下载的`proxyee-down-*.*.jar.zip`, 双击运行`main`下的`proxyee-down-core.jar`
2. 在任务栏找到`proxyee-down`, 点击安装证书
3. 弹出钥匙串访问, 找到刚刚添加的`proxyeeRoot`证书, 右键显示简介, 点开**信任**, 设置**使用此证书时**为**始终信任**
4. 打开`SwitchyOmega`选项, 新建**情景模式**, 选择**代理服务器**, 配置**代理协议**为`HTTP`, **代理服务器**为`127.0.0.1`, **代理端口**为`9999`, 点击**应用选项**保存
5. 使用代理打开百度, 干该干的事情

## 注意事项
1. 打开`proxyee-down-core.jar`时, 可能会被安全问题阻止, 请打开**安全性与隐私**-**通用**自行允许
2. 请保证`9000`及`9999`两端口无占用, 否则会使用另外一个端口进行代理, 具体自己查
3. 若使用了`SwitchyOmega`的自动切换模式, 请务必把`pan.baidu.com`及`www.baidupcs.com`两域名代理掉
4. 卸载删除同理, 删除`SwitchyOmega`, 删除证书, 删除源文件
