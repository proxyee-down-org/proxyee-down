### 常见问题
<details>
  <summary>安装完后进入百度云页面没有显示proxyee-down标记？</summary>
  
  **先尝试重启软件和浏览器，若还是不显示可以[安装switchy插件](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/switchy/read.md)进行下载**
</details>
<details>
  <summary>下载百度云出现pcs server refuse？</summary>
  
  **由于百度云服务器对批量下载限制了并发连接数，在有百度云批量下载任务时，再去创建批量下载的任务百度云就会检查到，若要下载可以暂停下载器正在下载的任务然后再创建。**
</details>
<details>
  <summary>下载百度云出现file not exist？</summary>
  
  **若是下载文件夹，文件夹或父级文件夹名称中不能含有+号，有+号的话请修改名称后再下载。**  
  **其他情况请尝试登录百度云再下载或在自己网盘新建一个文件夹再转存进来重新分享新文件夹再下载。**
</details>
<details>
  <summary>下载百度云出现sock connect error？</summary>
  
  **把浏览器地址栏的地址域名替换成yqall02.baidupcs.com或者d11.baidupcs.com再访问。**
</details>
<details>
  <summary>百度云下载速度太慢？</summary>
  
  **新建任务时调高分段数，若还是下载慢可以尝试将资源分享，然后退出帐号或者开启隐私窗口访问下载(避免帐号被限速)，**最好是单文件下载，批量下载现在可能会被限速而且可能会卡住下不动****
</details>
<details>
  <summary>百度云下载一直没有速度？</summary>
  
  **参考[#246](https://github.com/monkeyWie/proxyee-down/issues/246#issuecomment-378516262)**
</details>
<details>
  <summary>下载进度一直不动或任务状态显示失败怎么解决?</summary>
  
  **这种情况一般都是下载链接失效了，需要刷新下载链接，具体[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/refresh/read.md)**
</details>
<details>
  <summary>百度云下载的压缩文件打不开，提示文件损坏？</summary>
  
  **使用下载器工具里的百度云解压工具进行解压。**
</details>
<details>
  <summary>软件怎么更新到新版本？</summary>
  
  **在下载器的关于界面，可以进行在线升级。在线更新一直失败的话可以下载proxyee-down-x.xx-jar.zip的压缩包，解压main目录里的文件进行覆盖**
</details>
<details>
  <summary>运行时提示证书安装失败？</summary>
  
  **请按照教程里的[手动安装证书](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/ca/read.md)步骤进行安装**
</details>
<details>
  <summary>关闭软件后无法正常上网？</summary>
  
  **打开IE浏览器，将IE浏览器里的代理设置关闭即可。**
</details>
<details>
  <summary>是否支持断点下载？</summary>
  
  **支持。**
</details>