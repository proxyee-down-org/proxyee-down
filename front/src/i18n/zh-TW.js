export default {
  nav: {
    tasks: '任務管理',
    extension: '擴充管理',
    setting: '軟體設定',
    about: '關於專案',
    support: '支持我們'
  },
  tip: {
    tip: '提示',
    ok: '確定',
    cancel: '取消',
    notNull: '不能為空',
    fmtErr: '格式不正確',
    choose: '選擇',
    save: '儲存',
    copySucc: '複製成功',
    copyFail: '複製失敗'
  },
  tasks: {
    createTask: '建立任務',
    continueDownloading: '繼續下載',
    pauseDownloads: '暫停下載',
    deleteTask: '刪除任務',
    deleteTaskTip: '是否刪除任務和檔案？',
    method: '方法',
    url: '連結',
    fileName: '名稱',
    fileSize: '大小',
    connections: '連線數',
    filePath: '路徑',
    status: '狀態',
    operate: '操作',
    downloadAddress: '下載位址',
    downloadSpeed: '下載速度',
    createTime: '開始時間',
    taskProgress: '任務進度',
    wait: '待下載',
    unknowLeft: '不詳',
    statusPause: '暫停',
    statusFail: '失敗',
    statusDone: '完成',
    option: '附加',
    head: '要求標頭',
    body: '要求主體',
    detail: '下載細節',
    checkSameTask: '偵測到可能相同的下載任務，是否選擇任務進行更新？',
    sameTaskList: '任務清單',
    sameTaskPlaceholder: '請選擇要更新的任務'
  },
  extension: {
    conditions: '使用須知',
    conditionsContent: '首次使用擴充模組時，必須安裝由 Proxyee Down 隨機產生的一個 CA 憑證，點選下方的安裝按鈕並依系統的引導進行確認安裝。(注意：程式會在安裝前偵測作業系統中是否有安裝過憑證，當偵測到有安裝的情況會提示刪除對應的舊 CA 憑證)',
    install: '安裝',
    globalProxy: '全域代理',
    proxyTip: '點選檢視說明',
    copyPac: '複製 PAC 連結',
    title: '名稱',
    description: '描述',
    currVersion: '目前版本',
    newVersion: '最新版本',
    installStatus: '狀態',
    installStatusTrue: '已安装',
    installStatusFalse: '未安装',
    action: '操作',
    actionUpdate: '更新',
    actionInstall: '安裝',
    actionDetail: '細節',
    switch: '開關',
    downloadingTip: '下載中...[伺服器(',
    downloadOk: '下載成功',
    downloadErr: '下載失敗',
    downloadErrTip: '自動切換伺服器'
  },
  setting: {
    downSetting: '下載設定',
    path: '路徑',
    pathTip: '預設下載路徑',
    connections: '連線數',
    connectionsTip: '預設連線數',
    taskLimit: '同時下載任務數',
    taskSpeedLimit: '單任務限速',
    globalSpeedLimit: '全域限速',
    speedLimitTip: '0為不限速',
    appSetting: '系統設定',
    language: '語言',
    uiMode: 'UI 模式',
    uiModeWindows: '視窗',
    uiModeBrowser: '瀏覽器',
    checkUpdate: '檢查更新',
    checkUpdateWeek: '每週',
    checkUpdateStartup: '每次啟動',
    checkUpdateNever: '從不',
    secondProxy: {
      secondProxy: '二級代理',
      tip: '配置下載器的二級（前置）代理服務器',
      type: '類型',
      host: '服務器',
      port: '端口',
      user: '用戶名',
      pwd: '密碼'
    }
  },
  about: {
    project: {
      title: '項目',
      content: 'Proxyee Down 是一款開源的免費軟體，基於本軟體的高速下載核心和擴充套件，可以方便並快速的下載所需資源。',
      githubAddress: '項目首頁：',
      tutorial: '使用教學：',
      feedback: '問題回報：',
      currentVersion: '目前版本：'
    },
    team: {
      title: '團隊'
    }
  },
  update: {
    checkNew: '偵測到新版本',
    version: '版本號',
    changeLog: '更新內容',
    update: '更新',
    done: '更新完畢',
    restart: '是否重新啟動？',
    error: '更新失敗，請檢查網絡或手動下載更新包'
  },
  alert: {
    refused: '程式異常，拒絕存取',
    timeout: '程式異常，連線逾時',
    error: '程式出錯',
    notFound: '任務不存在',
    '/tasks': {
      post: {
        4000: '參數解析錯誤',
        4001: '要求對象不能為空',
        4002: '要求位址不能為空',
        4003: '檔案儲存路徑不能為空',
        4004: '建立資料夾失敗',
        4005: '無寫入權限',
        4006: '磁碟空間不足',
        4007: '檔案已存在'
      }
    },
    '/util/resolve': {
      put: {
        4000: '參數解析錯誤',
        4001: '要求位址不能為空',
        4002: '回應狀態碼異常',
        4003: '請求超時'
      }
    },
    '/config': {
      put: {
        4000: '參數解析錯誤'
      }
    }
  }
}