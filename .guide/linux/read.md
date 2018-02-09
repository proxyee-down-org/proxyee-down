## 安装环境 
- 进入[JRE下载页面](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html)，下载对应的JRE  
![1](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/linux/imgs/1.png)

- 以root用户登录将下载的jre-8u161-linux-x64.tar.gz文件放到/temp目录下，使用如下命令解压:
```tar zxvf jre-8u161-linux-x64.tar.gz```

- 将解压后的jre1.8.0_161复制到/opt下，命令如下：
```
root@test:~# mkdir /temp
root@test:~# cd /temp
root@test:/temp# tar zxvf jre-8u161-linux-x64.tar.gz 
root@test:/temp# cd /opt
root@test:/opt# mv /temp/jre1.8.0_161/ .
```

- 在/bin目录下创建java软链接
```
root@test:/opt# cd /bin
root@test:/bin# ln -s /opt/jre1.8.0_161/bin/java java
```
- 验证软件的正确性  
![5](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/linux/imgs/5.png)

- 配置Java环境
```
root@test:/bin# gedit /etc/profile

export JAVA_HOME=/opt/jre1.8.0_161
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=.:$CLASSPATH:$JAVA_HOME/lib:$JRE_HOME/lib   #这句容易出错
export PATH=$PATH:$JAVA_HOME/bin:$JRE_HOME/bin

root@test:/bin# source /etc/profile
root@test:/bin# echo $JAVA_HOME
/opt/jre1.8.0_161
```

## 解压并运行

将下载好的proxyee-down-x.xx-jar.zip解压至任意目录。运行以下两条命令：
```
ulimit -c unlimited
java -jar proxyee-down.jar
```
![10](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/linux/imgs/10.png)

## 证书安装
访问`127.0.0.1:9999`下载证书，选择第一条`Trust this CA to identify websites`，然后OK
![11](https://github.com/monkeyWie/proxyee-down/raw/master/.guide/linux/imgs/11.png)

## SwitchyOmega插件安装与设置

[查看](https://github.com/monkeyWie/proxyee-down/blob/master/.guide/common/switchy/read.md)

## 开启飞速下载
打开浏览器,选择要下载的资源进行下载就会弹出下载页面了。 


   
