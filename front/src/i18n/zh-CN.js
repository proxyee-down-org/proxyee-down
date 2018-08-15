export default {
  nav: {
    taskManager: '任务管理',
    toolList: '工具列表',
    softwareSetting: '软件设置',
    aboutProject: '关于项目',
    supportUs: '支持我们'
  },
  tasks: {
    createTasks: '创建任务',
    continueDownloading: '继续下载',
    pauseDownloads: '暂停下载',
    deleteTask: '删除任务',
    deleteTaskTip: '是否删除任务和文件？',
    fileName: '文件名',
    fileSize: '大小',
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
    statusDone: '完成'
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
    }
  }
}
