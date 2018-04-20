## 火狐浏览器安装证书
1. 右击软件托盘，点证书目录会跳转到对应目录，目录中的**ca.crt**文件为下载器的证书，记住证书的路径以备火狐浏览器导入。

![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/1-1.png)    
### 安装证书
1. 打开火狐浏览器并进入设置页面  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/2-1.png)  
2. 进入火狐浏览器证书管理页面  
在设置页面内找到隐私与安全->证书->查看证书，在打开的证书管理页面选中证书机构  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/2-2.png)  
3. 导入刚刚的证书
点击导入，选中刚刚的证书  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/2-3-1.png)  
把信任框都勾上点击确定  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/2-3-2.png)  
### 完成
证书导入完成，刷新页面即可
### 导入了证书还是提示不安全？
可能是证书导入时没有勾选信任框，进入火狐浏览器证书管理页面找到ProxyeeRoot证书,选中之后点击编辑信任，在弹出的页面里将信任框都勾上并确定。    
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/ca/firefox/imgs/4-1.png)  