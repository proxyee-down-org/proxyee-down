## 手动创建任务
在下载器任务面板点击左上角的+号，可手动创建任务。  
![](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/common/create/imgs/1-1.png)
### 链接
输入资源的下载链接(例如配合百度云获取直链的脚本，把获取到的下载链接粘贴在此输入框进行下载。)
### 请求头
可以自定义下载的请求头，请求头默认如下：  

key | value
---|---
Host | url域名
Connection | keep-alive
Upgrade-Insecure-Requests | 1
User-Agent | Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.75 Safari/537.36
Accept | text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8
Referer | url域名
Accept-Encoding | gzip, deflate, br
Accept-Language | zh-CN,zh;q=0.9

**当自定义的请求头与默认的key相同时，会覆盖默认请求头的value**
### 请求头
可以自定义下载的请求体，默认为空，仅支持文本。
## 创建成功
确认创建成功后，会弹出对应的下载页面。

