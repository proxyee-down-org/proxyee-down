export default {
  nav: {
    taskManager: 'Task Manager',
    toolList: 'Tool List',
    softwareSetting: 'Software Setting',
    aboutProject: 'About Project',
    supportUs: 'Support Us'
  },
  tasks: {
    createTasks: 'Create Tasks',
    continueDownloading: 'Continue Downloading',
    pauseDownloads: 'Pause Downloads',
    deleteTask: 'Delete Task',
    deleteTaskTip: 'Do you want to delete task and file?',
    fileName: 'Name',
    fileSize: 'Size',
    status: 'Status',
    operate: 'Operate',
    downloadAddress: 'Address',
    wait: 'Wait',
    unknowLeft: 'Unknow',
    downloadSpeed: 'Speed',
    createTime: 'Create Time',
    taskProgress: 'Schedule',
    statusPause: 'Pause',
    statusFail: 'Fail',
    statusDone: 'Done'
  },
  alert: {
    refused: 'Program exception,Connect Refused',
    timeout: 'Program exception,Connect Time Out',
    error: 'Program Error',
    notFound: 'Task not found',
    '/tasks': {
      post: {
        4000: 'Params parse error',
        4001: 'Request is empty',
        4002: 'Request URL is empty',
        4003: 'File save path is empty',
        4004: 'Failed to create folder',
        4005: 'No write permission',
        4006: 'Not enough disk space',
        4007: 'File already exists'
      }
    }
  }
}
