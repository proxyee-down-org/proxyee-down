## proxyee-down开放http接口
### 创建任务
选项 | 值
---|---
url | http://127.0.0.1:26339/open/createTask
method | POST  

### 请求
```
{
    "request": {
        "url": "",
        "heads": [],
        "body": ""
    },
    "fileName": "",
    "filePath": "",
    "connections": 1,
    "unzipFlag": 1,
    "unzipPath": ""
}
```
参数 | 描述 | 必要 | 默认值
---|---|---|---
request.url | 下载链接 | √ |
request.heads | 请求头 | × | 参考([手动创建任务](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/create/read.md))
request.body | 请求体 | × |
fileName | 下载文件名 | × | 下载链接响应的文件名
filePath | 下载路径 | √ |
connections | 分段数 | × | 配置的分段数
unzipFlag | 是否自动解压(1.是 0.否) | × | 1
unzipPath | 自动解压的路径 | × | 下载路径+文件名去后缀
### 响应
```
{
    "status": 200,
    "msg": "操作成功",
}
```
参数 | 描述 
---|---
status | 响应状态码(200.成功 400.参数错误 500.服务器异常)
msg | 响应提示信息



