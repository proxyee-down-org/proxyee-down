### 多连接分段HTTP下载器
自动嗅探HTTP下载请求，支持HTTP、HTTPS，可多连接分块下载，突破文件服务器单连接下载速度限制，基于[proxyee](https://github.com/monkeyWie/proxyee)实现。
### HTTPS支持
    需要导入项目中的CA证书(src/resources/ca.crt)至受信任的根证书颁发机构.
### 运行
```
new HttpDownServer().start(9999);
```