## proxyee-down开放http接口
### 创建任务
选项 | 值
---|---
api | http://127.0.0.1:26339/api/rpc/createTask
method | POST
Content-Type | application/json
参数
```
{
    "connections": 1,
    "fileName": "",
    "filePath": "f:/down",
    "request": {
        "body": "",
        "heads": [],
        "url": "http://192.168.2.24/test4.zip"
    },
    "unzipFlag": 1,
    "unzipPath": ""
}
```
