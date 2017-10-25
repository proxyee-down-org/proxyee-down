### java实现HTTP下载器
    支持HTTP、HTTPS分段多连接下载，基于[proxyee](https://github.com/monkeyWie/proxyee)的http代理功能实现监听文件的下载。
### HTTPS支持
    需要导入项目中的CA证书(src/resources/ca.crt)至受信任的根证书颁发机构.
### 运行
```
new HttpDownServer().start(9999);
```