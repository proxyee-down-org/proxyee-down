export default {
  nav: {
    tasks: '任务管理',
    extension: '扩展管理',
    setting: '软件设置',
    about: '关于项目',
    support: '支持我们'
  },
  tip: {
    tip: '提示',
    ok: '确定',
    cancel: '取消',
    notNull: '不能为空',
    fmtErr: '格式不正确',
    choose: '选择',
    save: '保存',
    copySucc: '复制成功',
    copyFail: '复制失败'
  },
  tasks: {
    createTask: '创建任务',
    continueDownloading: '继续下载',
    pauseDownloads: '暂停下载',
    deleteTask: '删除任务',
    deleteTaskTip: '是否删除任务和文件？',
    method: '方法',
    url: '链接',
    fileName: '文件名',
    fileSize: '大小',
    connections: '连接数',
    filePath: '路径',
    status: '状态',
    operate: '操作',
    downloadAddress: '下载地址',
    downloadSpeed: '下载速度',
    createTime: '开始时间',
    taskProgress: '任务进度',
    wait: '待下载',
    unknowLeft: '未知',
    statusPause: '暂停',
    statusFail: '失败',
    statusDone: '完成',
    option: '附加',
    head: '请求头',
    body: '请求体',
    detail: '下载详情',
    checkSameTask: '检测到可能相同的下载任务，是否选择任务进行刷新？',
    sameTaskList: '任务列表',
    sameTaskPlaceholder: '请选择要刷新的任务'
  },
  extension: {
    conditions: '使用须知',
    conditionsContent: '首次使用扩展模块时，必须安装由Proxyee Down随机生成的一个CA证书，点击下面的安装按钮并按系统的引导进行确认安装。(注意：程序会在安装前检测操作系统中是否有安装过证书，当检测到有安装的情况会提示删除对应的旧CA证书)',
    install: '安装',
    globalProxy: '全局代理',
    proxyTip: '点击查看说明',
    copyPac: '复制PAC链接',
    title: '名称',
    description: '描述',
    currVersion: '当前版本',
    newVersion: '最新版本',
    installStatus: '状态',
    installStatusTrue: '已安装',
    installStatusFalse: '未安装',
    action: '操作',
    actionUpdate: '更新',
    actionInstall: '安装',
    actionDetail: '详情',
    switch: '开关',
    downloadingTip: '下载中...[服务器(',
    downloadOk: '下载成功',
    downloadErr: '下载失败',
    downloadErrTip: '自动切换服务器'
  },
  setting: {
    downSetting: '下载设置',
    path: '路径',
    pathTip: '默认下载路径',
    connections: '连接数',
    connectionsTip: '默认连接数',
    taskLimit: '同时下载任务数',
    taskSpeedLimit: '单任务限速',
    globalSpeedLimit: '全局限速',
    speedLimitTip: '0为不限速',
    appSetting: '系统设置',
    language: '语言',
    uiMode: 'UI模式',
    uiModeWindows: '窗口',
    uiModeBrowser: '浏览器',
    checkUpdate: '检查更新',
    checkUpdateWeek: '每周',
    checkUpdateStartup: '每次启动',
    checkUpdateNever: '从不',
    secondProxy: {
      secondProxy: '二级代理',
      tip: '配置下载器的二级(前置)代理服务器',
      type: '类型',
      host: '服务器',
      port: '端口',
      user: '用户名',
      pwd: '密码'
    }
  },
  about: {
    project: {
      title: '项目',
      content: 'Proxyee Down是一款开源的免费软件，基于本软件的高速下载内核和扩展，可以方便并快速的下载所需资源。',
      githubAddress: '项目主页：',
      tutorial: '使用教程：',
      feedback: '问题反馈：',
      currentVersion: '当前版本：'
    },
    team: {
      title: '团队'
    }
  },
  update: {
    checkNew: '检测到新版本',
    version: '版本号',
    changeLog: '更新内容',
    update: '更新',
    done: '更新完毕',
    restart: '是否重新启动？',
    error: '更新失败，请检查网络或手动下载更新包'
  },
  alert: {
    refused: '程序异常，拒绝访问',
    timeout: '程序异常，连接超时',
    error: '程序出错',
    notFound: '任务不存在',
    '/tasks': {
      post: {
        4000: '参数解析错误',
        4001: '请求对象不能为空',
        4002: '请求地址不能为空',
        4003: '文件保存路径不能为空',
        4004: '创建文件夹失败',
        4005: '无写入权限',
        4006: '磁盘空间不足',
        4007: '文件已存在'
      }
    },
    '/util/resolve': {
      put: {
        4000: '参数解析错误',
        4001: '请求地址不能为空',
        4002: '响应状态码异常',
        4003: '请求超时'
      }
    },
    '/config': {
      put: {
        4000: '参数解析错误'
      }
    }
  }
}