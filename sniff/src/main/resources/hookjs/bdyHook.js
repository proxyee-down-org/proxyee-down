//1.9
var initHookInterval = setInterval(function () {
  if (!window.$) {
    return;
  }
  if ($('.module-header-wrapper dl:first').find('dd:first').length == 0) {
    return;
  }
  clearInterval(initHookInterval);
  //插件加载提示
  (function () {
    var pd_dd = $('.module-header-wrapper dl:first').find('dd:first');
    if (pd_dd.find(">span").attr("class") && pd_dd.find(">span>span").attr(
            "class")) {
      var pd_parent_span_class = pd_dd.find(">span").attr("class").split(
          " ")[0];
      var pd_child_span_class = pd_dd.find(">span>span").attr("class").split(
          " ")[0];
      $('.module-header-wrapper dl:first').find('dd:first').append(
          '<span class="' + pd_parent_span_class + ' find-light">'
          + '<a href="javascript:alert(\'请在网盘中选择文件再点击下载按钮(此按钮非下载按钮，仅用于提示)\')">proxyee-down</a>'
          + '<span class="' + pd_child_span_class + '"></span>'
          + '<i class="find-light-icon"></i>'
          + '</span>');
    }
  })();
  (function () {
    'use strict';

    var $ = $ || window.$;
    var wordMapHttp = {
      'list-grid-switch': 'yvgb9XJ',
      'list-switched-on': 'ksbXZm',
      'grid-switched-on': 'tch6W25',
      'list-switch': 'lrbo9a',
      'grid-switch': 'xh6poL',
      'checkbox': 'EOGexf',
      'col-item': 'Qxyfvg',
      'check': 'fydGNC',
      'checked': 'EzubGg',
      'list-view': 'vdAfKMb',
      'item-active': 'ngb9O6',
      'grid-view': 'JKvHJMb',
      'bar-search': 'OFaPaO',
      'default-dom': 'xpX2PV',
      'bar': 'qxnX2G5',
      'list-tools': 'QDDOQB'
    };
    var wordMapHttps = {
      'list-grid-switch': 'qobmXB1q',
      'list-switched-on': 'ewXm1e',
      'grid-switched-on': 'kxhkX2Em',
      'list-switch': 'rvpXm63',
      'grid-switch': 'mxgdJgwv',
      'checkbox': 'EOGexf',
      'col-item': 'Qxyfvg',
      'check': 'fydGNC',
      'checked': 'EzubGg',
      'list-view': 'vdAfKMb',
      'item-active': 'pcamXBRX',
      'grid-view': 'JKvHJMb',
      'bar-search': 'OFaPaO',
      'default-dom': 'nyztJqWE',
      'bar': 'mkseJqKQ',
      'list-tools': 'QDDOQB'
    };
    var wordMap = location.protocol == 'http:' ? wordMapHttp : wordMapHttps;

    function getDefaultStyle(obj, attribute) {
      return obj.currentStyle ? obj.currentStyle[attribute]
          : document.defaultView.getComputedStyle(obj, false)[attribute];
    }

    $(function () {
      switch (detectPage()) {
        case 'disk':
          var panHelper = new PanHelper();
          panHelper.init();
          return;
        case 'share':
        case 's':
          var panShareHelper = new PanShareHelper();
          panShareHelper.init();
          return;
        default:
          return;
      }
    });

    //检查下载的文件中是否包含带+名称的文件夹
    function checkFileName(list) {
      var ret = true;
      if (list && list.length > 0) {
        for (var i = 0; i < list.length; i++) {
          if (list[i].isdir == 1 && list[i].filename.indexOf("+") != -1) {
            ret = false;
            break;
          }
        }
      }
      return ret;
    }

    //网盘页面的下载助手
    function PanHelper() {
      var yunData, sign, timestamp, bdstoken, logid, fid_list;
      var fileList = [], selectFileList = [], list_grid_status = 'list';
      var observer, currentPage, currentPath, currentCategory, dialog,
          searchKey;
      var panAPIUrl = location.protocol + "//" + location.host + "/api/";

      this.init = function () {
        yunData = window.yunData;
        initParams();
        registerEventListener();
        createObserver();
        createIframe();
        dialog = new Dialog({addCopy: true});
      };

      function initParams() {
        sign = getSign();
        timestamp = getTimestamp();
        bdstoken = getBDStoken();
        logid = getLogID();
        currentPage = getCurrentPage();

        if (currentPage == 'all') {
          currentPath = getPath();
        }

        if (currentPage == 'category') {
          currentCategory = getCategory();
        }

        if (currentPage == 'search') {
          searchKey = getSearchKey();
        }

        refreshListGridStatus();
        refreshFileList();
        refreshSelectList();
      }

      function refreshFileList() {
        if (currentPage == 'all') {
          fileList = getFileList();
        } else if (currentPage == 'category') {
          fileList = getCategoryFileList();
        } else if (currentPage == 'search') {
          fileList = getSearchFileList();
        }
      }

      function refreshSelectList() {
        selectFileList = [];
      }

      function refreshListGridStatus() {
        list_grid_status = getListGridStatus();
      }

      //获取当前的视图模式
      function getListGridStatus() {
        //return $('div.list-grid-switch').hasClass('list-switched-on')?'list':($('div.list-grid-switch').hasClass('grid-switched-on')?'grid':'list');
        //return $('div.itiWzPY').hasClass('kudtWY46')?'list':($('div.itiWzPY').hasClass('nytAL9w')?'grid':'list');
        return $('div.' + wordMap['list-grid-switch']).hasClass(
            wordMap['list-switched-on']) ? 'list' : ($('div.'
            + wordMap['list-grid-switch']).hasClass(wordMap['grid-switched-on'])
            ? 'grid' : 'list');
      }

      function registerEventListener() {
        registerHashChange();
        registerListGridStatus();
        registerDownButton();
      }

      //监视地址栏#标签的变化
      function registerHashChange() {
        window.addEventListener('hashchange', function (e) {
          obFlag = false;
          refreshListGridStatus();
          if (getCurrentPage() == 'all') {
            if (currentPage == getCurrentPage()) {
              if (currentPath == getPath()) {
                return;
              } else {
                currentPath = getPath();
                refreshFileList();
                refreshSelectList();
              }
            } else {
              currentPage = getCurrentPage();
              currentPath = getPath();
              refreshFileList();
              refreshSelectList();
            }
          } else if (getCurrentPage() == 'category') {
            if (currentPage == getCurrentPage()) {
              if (currentCategory == getCategory()) {
                return;
              } else {
                currentPage = getCurrentPage();
                currentCategory = getCategory();
                refreshFileList();
                refreshSelectList();
              }
            } else {
              currentPage = getCurrentPage();
              currentCategory = getCategory();
              refreshFileList();
              refreshSelectList();
            }
          } else if (getCurrentPage() == 'search') {
            if (currentPage == getCurrentPage()) {
              if (searchKey == getSearchKey()) {
                return;
              } else {
                currentPage = getCurrentPage();
                searchKey = getSearchKey();
                refreshFileList();
                refreshSelectList();
              }
            } else {
              currentPage = getCurrentPage();
              searchKey = getSearchKey();
              refreshFileList();
              refreshSelectList();
            }
          }
        });
      }

      //监视视图变化
      function registerListGridStatus() {
        //var $a_list = $('a[node-type=list-switch]');
        //var $a_list = $('a[node-type=eepWzkk]');
        var $a_list = $('a[node-type=' + wordMap['list-switch'] + ']');
        $a_list.click(function () {
          list_grid_status = 'list';
        });

        //var $a_grid = $('a[node-type=grid-switch]');
        //var $a_grid = $('a[node-type=ytnvWY7q]');
        var $a_grid = $('a[node-type=' + wordMap['grid-switch'] + ']');
        $a_grid.click(function () {
          list_grid_status = 'grid';
        });
      }

      //监视文件下载
      function registerDownButton(isGrid) {
        if (isGrid) {
          $('div.context-menu li:eq(1)').unbind('click').click(function (e) {
            linkClick(e, isGrid);
            e.preventDefault();
            e.stopPropagation();
          });
        } else {
          var checkInterval = setInterval(function () {
            if ($('a.g-button[title^=下载]>span').length > 1) {
              clearInterval(checkInterval);
              $('a.g-button[title^=下载]>span').unbind('click')
              .click(function (e) {
                linkClick(e, isGrid);
                e.preventDefault();
                e.stopPropagation();
              });
            }
          }, 500);
        }
      }

      var obFlag = false;

      //监视文件列表显示变化
      function createObserver() {
        var options = {
          'childList': true,
        };
        var downButtonObServer = new MutationObserver(function (mutations) {
          registerDownButton(false);
        });
        var observer = new MutationObserver(function (mutations) {
          if (mutations.length == 1
              && mutations[0].addedNodes
              && mutations[0].addedNodes.length > 0
              && $(mutations[0].addedNodes[0]).attr('class')
              == 'context-menu') {
            registerDownButton(true);
          } else {
            if (!obFlag) {
              obFlag = true;
              $('div.' + wordMap['list-view'] + ' div.operate').each(
                  function () {
                    downButtonObServer.observe(this, options);
                  });
            }
          }
        });

        var list_view = document.querySelector('.' + wordMap['list-view']);
        observer.observe(list_view, options);
        observer.observe(document.body, options);
      }

      function linkClick(e, isGrid) {
        selectFileList = [];
        var selectFileName = '';
        if (isGrid) {
          selectFileName = $('div.' + wordMap['grid-view']
              + ' div.open-enable.zbmh0WQ7 div.file-name>a').text();
        } else {
          selectFileName = $(e.target).parents('.file-name').find(
              'div.text>a').text();
        }
        if (selectFileName) {
          $.each(fileList, function (i, file) {
            if (file.server_filename == selectFileName) {
              selectFileList.push({
                filename: file.server_filename,
                path: file.path,
                fs_id: file.fs_id,
                isdir: file.isdir
              });
              return false;
            }
          });
        } else {
          $('span.' + wordMap['checkbox']).parent().each(function () {
            if (getDefaultStyle($(this).find(">span>span").get(0), 'display')
                != 'none') {
              var fileName = $(this).find('div.file-name div.text>a').text();
              $.each(fileList, function (i, file) {
                if (file.server_filename == fileName) {
                  selectFileList.push({
                    filename: file.server_filename,
                    path: file.path,
                    fs_id: file.fs_id,
                    isdir: file.isdir
                  });
                  return false;
                }
              });
            }
          });
        }
        var downloadType;
        var downloadLink;
        if (selectFileList.length === 0) {
          alert("获取选中文件失败，请刷新重试！");
          return;
        } else if (selectFileList.length == 1) {
          if (selectFileList[0].isdir === 1) {
            downloadType = 'batch';
          } else if (selectFileList[0].isdir === 0) {
            downloadType = 'dlink';
          }
        } else if (selectFileList.length > 1) {
          downloadType = 'batch';
        }
        if (selectFileList.length >= 1000) {
          alert("由于百度云的限制，批量下载选中的文件数量不能超过1000，请分批下载");
          return;
        }
        if (!checkFileName(selectFileList)) {
          alert("文件夹名称不能包含+号，请修改名称后再下载");
          return;
        }
        fid_list = getFidList(selectFileList);
        var result = getDownloadLinkWithPanAPI(downloadType);
        if (result.errno === 0) {
          if (downloadType == 'dlink') {
            downloadLink = result.dlink[0].dlink;
          } else if (downloadType == 'batch') {
            downloadLink = result.dlink;
            if (selectFileList.length === 1) {
              downloadLink = downloadLink + '&zipname=' + encodeURIComponent(
                  selectFileList[0].filename) + '.zip';
            }
          }
          else {
            alert("发生错误！");
            return;
          }
        } else if (result.errno == -1) {
          alert('文件不存在或已被百度和谐，无法下载！');
          return;
        } else if (result.errno == 112) {
          alert("页面过期，请刷新重试！");
          return;
        } else {
          alert("发生错误！");
          return;
        }
        window.open(downloadLink)
      }

      function getSign() {
        var signFnc;
        try {
          signFnc = new Function("return " + yunData.sign2)();
        } catch (e) {
          throw new Error(e.message);
        }
        return base64Encode(signFnc(yunData.sign5, yunData.sign1));
      }

      //获取当前目录
      function getPath() {
        var hash = location.hash;
        var regx = /(^|&|\/|\?)path=([^&]*)(&|$)/i;
        var result = hash.match(regx);
        return decodeURIComponent(result[2]);
      }

      //获取分类显示的类别，即地址栏中的type
      function getCategory() {
        var hash = location.hash;
        var regx = /(^|&|\/|\?)type=([^&]*)(&|$)/i;
        var result = hash.match(regx);
        return decodeURIComponent(result[2]);
      }

      function getSearchKey() {
        var hash = location.hash;
        var regx = /(^|&|\/)key=([^&]*)(&|$)/i;
        var result = hash.match(regx);
        return decodeURIComponent(result[2]);
      }

      //获取当前页面(list或者category)
      function getCurrentPage() {
        var hash = location.hash;
        return decodeURIComponent(
            hash.substring(hash.indexOf('#/') + 2, hash.indexOf('?')));
      }

      //获取文件列表
      function getFileList() {
        var filelist = [];
        var listUrl = panAPIUrl + "list";
        var path = getPath();
        logid = getLogID();
        var params = {
          dir: path,
          bdstoken: bdstoken,
          logid: logid,
          order: 'size',
          desc: 0,
          clienttype: 0,
          showempty: 0,
          web: 1,
          channel: 'chunlei',
          appid: 250528
        };
        $.ajax({
          url: listUrl,
          async: false,
          method: 'GET',
          data: params,
          success: function (response) {
            filelist = 0 === response.errno ? response.list : [];
          }
        });
        return filelist;
      }

      //获取分类页面下的文件列表
      function getCategoryFileList() {
        var filelist = [];
        var listUrl = panAPIUrl + "categorylist";
        var category = getCategory();
        logid = getLogID();
        var params = {
          category: category,
          bdstoken: bdstoken,
          logid: logid,
          order: 'size',
          desc: 0,
          clienttype: 0,
          showempty: 0,
          web: 1,
          channel: 'chunlei',
          appid: 250528
        };
        $.ajax({
          url: listUrl,
          async: false,
          method: 'GET',
          data: params,
          success: function (response) {
            filelist = 0 === response.errno ? response.info : [];
          }
        });
        return filelist;
      }

      function getSearchFileList() {
        var filelist = [];
        var listUrl = panAPIUrl + 'search';
        logid = getLogID();
        searchKey = getSearchKey();
        var params = {
          recursion: 1,
          order: 'time',
          desc: 1,
          showempty: 0,
          web: 1,
          page: 1,
          num: 100,
          key: searchKey,
          channel: 'chunlei',
          app_id: 250528,
          bdstoken: bdstoken,
          logid: logid,
          clienttype: 0
        };
        $.ajax({
          url: listUrl,
          async: false,
          method: 'GET',
          data: params,
          success: function (response) {
            filelist = 0 === response.errno ? response.list : [];
          }
        });
        return filelist;
      }

      //生成下载时的fid_list参数
      function getFidList(list) {
        var fidlist = null;
        if (list.length === 0) {
          return null;
        }
        var fileidlist = [];
        $.each(list, function (index, element) {
          fileidlist.push(element.fs_id);
        });
        fidlist = '[' + fileidlist + ']';
        return fidlist;
      }

      function getTimestamp() {
        return yunData.timestamp;
      }

      function getBDStoken() {
        return yunData.MYBDSTOKEN;
      }

      //获取直接下载地址
      //这个地址不是直接下载地址，访问这个地址会返回302，response header中的location才是真实下载地址
      //暂时没有找到提取方法
      function getDownloadLinkWithPanAPI(type) {
        var downloadUrl = panAPIUrl + "download";
        var result;
        logid = getLogID();
        var params = {
          sign: sign,
          timestamp: timestamp,
          fidlist: fid_list,
          type: type,
          channel: 'chunlei',
          web: 1,
          app_id: 250528,
          bdstoken: bdstoken,
          logid: logid,
          clienttype: 0
        };
        $.ajax({
          url: downloadUrl,
          async: false,
          method: 'GET',
          data: params,
          success: function (response) {
            result = response;
          }
        });
        return result;
      }

      function createIframe() {
        var $div = $(
            '<div class="helper-hide" style="padding:0;margin:0;display:block"></div>');
        var $iframe = $(
            '<iframe src="javascript:void(0)" id="helperdownloadiframe" style="display:none"></iframe>');
        $div.append($iframe);
        $('body').append($div);

      }
    }

    //分享页面的下载助手
    function PanShareHelper() {
      var yunData, sign, timestamp, bdstoken, channel, clienttype, web, app_id,
          logid, encrypt, product, uk, primaryid, fid_list, extra, shareid;
      var vcode;
      var shareType, buttonTarget, currentPath, list_grid_status, observer,
          dialog, vcodeDialog;
      var fileList = [], selectFileList = [];
      var panAPIUrl = location.protocol + "//" + location.host + "/api/";
      var shareListUrl = location.protocol + "//" + location.host
          + "/share/list";

      this.init = function () {
        yunData = window.yunData;
        initParams();
        dialog = new Dialog({addCopy: false});
        vcodeDialog = new VCodeDialog(refreshVCode, confirmClick);
        createIframe();
        if (!isSingleShare()) {
          registerEventListener();
          createObserver();
        }
        registerDownButton();
      };

      function initParams() {
        shareType = getShareType();
        sign = yunData.SIGN;
        timestamp = yunData.TIMESTAMP;
        bdstoken = yunData.MYBDSTOKEN;
        channel = 'chunlei';
        clienttype = 0;
        web = 1;
        app_id = 250528;
        logid = getLogID();
        encrypt = 0;
        product = 'share';
        primaryid = yunData.SHARE_ID;
        uk = yunData.SHARE_UK;

        if (shareType == 'secret') {
          extra = getExtra();
        }
        if (isSingleShare()) {
          var obj = {};
          if (yunData.CATEGORY == 2) {
            obj.filename = yunData.FILENAME;
            obj.path = yunData.PATH;
            obj.fs_id = yunData.FS_ID;
            obj.isdir = 0;
          } else {
            obj.filename = yunData.FILEINFO[0].server_filename,
                obj.path = yunData.FILEINFO[0].path,
                obj.fs_id = yunData.FILEINFO[0].fs_id,
                obj.isdir = yunData.FILEINFO[0].isdir
          }
          selectFileList.push(obj);
        } else {
          shareid = yunData.SHARE_ID;
          currentPath = getPath();
          list_grid_status = getListGridStatus();
          fileList = getFileList();
        }
      }

      //判断分享类型（public或者secret）
      function getShareType() {
        return yunData.SHARE_PUBLIC === 1 ? 'public' : 'secret';
      }

      //判断是单个文件分享还是文件夹或者多文件分享
      function isSingleShare() {
        return yunData.getContext === undefined ? true : false;
      }

      function getExtra() {
        var seKey = decodeURIComponent(getCookie('BDCLND'));
        return '{' + '"sekey":"' + seKey + '"' + "}";
      }

      //获取当前目录
      function getPath() {
        var hash = location.hash;
        var regx = /(^|&|\/|\?)path=([^&]*)(&|$)/i;
        var result = hash.match(regx);
        return decodeURIComponent(result[2]);
      }

      //获取当前的视图模式
      function getListGridStatus() {
        var status = 'list';
        var $status_div = $('div.list-grid-switch');
        if ($status_div.hasClass('list-switched-on')) {
          status = 'list';
        } else if ($status_div.hasClass('grid-switched-on')) {
          status = 'grid';
        }
        return status;
      }

      function createIframe() {
        var $div = $(
            '<div class="helper-hide" style="padding:0;margin:0;display:block"></div>');
        var $iframe = $(
            '<iframe src="javascript:void(0)" id="helperdownloadiframe" style="display:none"></iframe>');
        $div.append($iframe);
        $('body').append($div);
      }

      function registerEventListener() {
        registerHashChange();
        registerListGridStatus();
      }

      //监视地址栏#标签变化
      function registerHashChange() {
        window.addEventListener('hashchange', function (e) {
          list_grid_status = getListGridStatus();
          if (currentPath == getPath()) {
            return;
          } else {
            currentPath = getPath();
            refreshFileList();
            refreshSelectFileList();
          }
        });
      }

      function refreshFileList() {
        fileList = getFileList();
      }

      function refreshSelectFileList() {
        selectFileList = [];
      }

      //监视视图变化
      function registerListGridStatus() {
        var $a_list = $('a[node-type=list-switch]');
        $a_list.click(function () {
          list_grid_status = 'list';
        });

        var $a_grid = $('a[node-type=grid-switch]');
        $a_grid.click(function () {
          list_grid_status = 'grid';
        });
      }

      //监视文件选择框
      function registerDownButton() {
        $('a.g-button[title^=下载]>span').unbind('click');
        $('a.g-button[title^=下载]>span').click(function (e) {
          linkButtonClick(e);
          e.preventDefault();
          e.stopPropagation();
        });
      }

      //监视文件列表显示变化
      function createObserver() {
        var MutationObserver = window.MutationObserver;
        var options = {
          'childList': true
        };
        observer = new MutationObserver(function (mutations) {
          registerDownButton();
        });
        //var list_view = document.querySelector('.list-view');
        //var grid_view = document.querySelector('.grid-view');

        var list_view = document.querySelector('.' + wordMap['list-view']);
        var grid_view = document.querySelector('.' + wordMap['grid-view']);

        observer.observe(list_view, options);
        observer.observe(grid_view, options);
      }

      //获取文件信息列表
      function getFileList() {
        var result = [];
        if (getPath() == '/') {
          result = yunData.FILEINFO;
        } else {
          logid = getLogID();
          var params = {
            uk: uk,
            shareid: shareid,
            order: 'other',
            desc: 1,
            showempty: 0,
            web: web,
            dir: getPath(),
            t: Math.random(),
            bdstoken: bdstoken,
            channel: channel,
            clienttype: clienttype,
            app_id: app_id,
            logid: logid
          };
          $.ajax({
            url: shareListUrl,
            method: 'GET',
            async: false,
            data: params,
            success: function (response) {
              if (response.errno === 0) {
                result = response.list;
              }
            }
          });
        }
        return result;
      }

      //获取验证码
      function getVCode() {
        var url = panAPIUrl + 'getvcode';
        var result;
        logid = getLogID();
        var params = {
          prod: 'pan',
          t: Math.random(),
          bdstoken: bdstoken,
          channel: channel,
          clienttype: clienttype,
          web: web,
          app_id: app_id,
          logid: logid
        };
        $.ajax({
          url: url,
          method: 'GET',
          async: false,
          data: params,
          success: function (response) {
            result = response;
          }
        });
        return result;
      }

      //刷新验证码
      function refreshVCode() {
        vcode = getVCode();
        $('#dialog-img').attr('src', vcode.img);
      }

      //验证码确认提交
      function confirmClick() {
        var val = $('#dialog-input').val();
        if (val.length === 0) {
          $('#dialog-err').text('请输入验证码');
          return;
        } else if (val.length < 4) {
          $('#dialog-err').text('验证码输入错误，请重新输入');
          return;
        }
        var result = getDownloadLinkWithVCode(val);
        if (result.errno == -20) {
          vcodeDialog.close();
          $('#dialog-err').text('验证码输入错误，请重新输入');
          refreshVCode();
          if (!vcode || vcode.errno !== 0) {
            alert('获取验证码失败！');
            return;
          }
          vcodeDialog.open();
        } else if (result.errno === 0) {
          vcodeDialog.close();
          var downloadLink;
          if (selectFileList.length == 1 && selectFileList[0].isdir === 0) {
            downloadLink = result.list[0].dlink;
          } else {
            downloadLink = result.dlink;
          }
          window.open(downloadLink)
        } else {
          alert('发生错误！');
          return;
        }
      }

      //生成下载用的fid_list参数
      function getFidList() {
        var fidlist = [];
        $.each(selectFileList, function (index, element) {
          fidlist.push(element.fs_id);
        });
        return '[' + fidlist + ']';
      }

      function linkButtonClick(e) {
        selectFileList = [];
        var selectFile = $(e.target).parents('.file-name');
        if (selectFile.length > 0) {
          $.each(fileList, function (i, file) {
            if (file.server_filename == selectFile.find('div.text>a').text()) {
              selectFileList.push({
                filename: file.server_filename,
                path: file.path,
                fs_id: file.fs_id,
                isdir: file.isdir
              });
              return false;
            }
          });
        } else {
          var fileInfo = yunData.FILEINFO[0] || {
            server_filename: yunData.FILENAME,
            path: yunData.PATH,
            fs_id: yunData.FS_ID,
            isdir: 0
          };
          if (yunData.FILEINFO.length <= 1 && fileInfo.isdir == 0) {
            selectFileList.push({
              filename: fileInfo.server_filename,
              path: fileInfo.path,
              fs_id: fileInfo.fs_id,
              isdir: fileInfo.isdir
            });
          } else {
            $('span.' + wordMap['checkbox']).parent().filter(
                '.JS-item-active').find('a.filename').each(function () {
              var _this = $(this);
              $.each(fileList, function (i, file) {
                if (file.server_filename == _this.text()) {
                  selectFileList.push({
                    filename: file.server_filename,
                    path: file.path,
                    fs_id: file.fs_id,
                    isdir: file.isdir
                  });
                  return false;
                }
              });
            });
          }
        }
        if (selectFileList.length === 0) {
          alert('获取选中文件失败，请刷新重试');
          return;
        }
        if (selectFileList.length >= 1000) {
          alert("由于百度云的限制，批量下载选中的文件数量不能超过1000，请分批下载");
          return;
        }
        if (!checkFileName(selectFileList)) {
          alert("文件夹名称不能包含+号，请修改名称后再下载");
          return;
        }
        buttonTarget = 'link';
        var downloadInfo = getDownloadLink();

        if (downloadInfo.errno == -20) {
          vcode = getVCode();
          if (!vcode || vcode.errno !== 0) {
            alert('获取验证码失败！');
            return;
          }
          vcodeDialog.open(vcode);
        } else if (downloadInfo.errno == 112) {
          alert('页面过期，请刷新重试');
          return;
        } else if (downloadInfo.errno === 0) {
          var downloadLink;
          if (selectFileList.length == 1 && selectFileList[0].isdir === 0) {
            downloadLink = downloadInfo.list[0].dlink;
          } else {
            downloadLink = downloadInfo.dlink;
          }
          var filename = '';
          $.each(selectFileList, function (index, element) {
            if (selectFileList.length == 1) {
              filename = element.filename;
            } else {
              if (index == 0) {
                filename = element.filename;
              } else {
                filename = filename + ',' + element.filename;
              }
            }
          });
          window.open(downloadLink)
        } else {
          alert('获取下载链接失败！');
          return;
        }
      }

      //获取下载链接
      function getDownloadLink() {
        var result;
        if (isSingleShare) {
          fid_list = getFidList();
          logid = getLogID();
          var url = panAPIUrl + 'sharedownload?sign=' + sign + '&timestamp='
              + timestamp + '&bdstoken=' + bdstoken + '&channel=' + channel
              + '&clienttype=' + clienttype + '&web=' + web + '&app_id='
              + app_id + '&logid=' + logid;
          var params = {
            encrypt: encrypt,
            product: product,
            uk: uk,
            primaryid: primaryid,
            fid_list: fid_list
          };
          if (shareType == 'secret') {
            params.extra = extra;
          }
          if (selectFileList[0].isdir == 1 || selectFileList.length > 1) {
            params.type = 'batch';
          }
          $.ajax({
            url: url,
            method: 'POST',
            async: false,
            data: params,
            success: function (response) {
              result = response;
            }
          });
        }
        return result;
      }

      //有验证码输入时获取下载链接
      function getDownloadLinkWithVCode(vcodeInput) {
        var result;
        if (isSingleShare) {
          fid_list = getFidList();
          var url = panAPIUrl + 'sharedownload?sign=' + sign + '&timestamp='
              + timestamp + '&bdstoken=' + bdstoken + '&channel=' + channel
              + '&clienttype=' + clienttype + '&web=' + web + '&app_id='
              + app_id + '&logid=' + logid;
          var params = {
            encrypt: encrypt,
            product: product,
            vcode_input: vcodeInput,
            vcode_str: vcode.vcode,
            uk: uk,
            primaryid: primaryid,
            fid_list: fid_list
          };
          if (shareType == 'secret') {
            params.extra = extra;
          }
          if (selectFileList[0].isdir == 1 || selectFileList.length > 1) {
            params.type = 'batch';
          }
          $.ajax({
            url: url,
            method: 'POST',
            async: false,
            data: params,
            success: function (response) {
              result = response;
            }
          });
        }
        return result;
      }
    }

    function base64Encode(t) {
      var a, r, e, n, i, s,
          o = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
      for (e = t.length, r = 0, a = ""; e > r;) {
        if (n = 255 & t.charCodeAt(r++), r == e) {
          a += o.charAt(n >> 2);
          a += o.charAt((3 & n) << 4);
          a += "==";
          break;
        }
        if (i = t.charCodeAt(r++), r == e) {
          a += o.charAt(n >> 2);
          a += o.charAt((3 & n) << 4 | (240 & i) >> 4);
          a += o.charAt((15 & i) << 2);
          a += "=";
          break;
        }
        s = t.charCodeAt(r++);
        a += o.charAt(n >> 2);
        a += o.charAt((3 & n) << 4 | (240 & i) >> 4);
        a += o.charAt((15 & i) << 2 | (192 & s) >> 6);
        a += o.charAt(63 & s);
      }
      return a;
    }

    function detectPage() {
      var regx = /[\/].+[\/]/g;
      var page = location.pathname.match(regx);
      return page[0].replace(/\//g, '');
    }

    function getCookie(e) {
      var o, t;
      var n = document, c = decodeURI;
      return n.cookie.length > 0 && (o = n.cookie.indexOf(e + "="), -1 != o)
          ? (o = o + e.length + 1, t = n.cookie.indexOf(";", o), -1 == t
          && (t = n.cookie.length), c(n.cookie.substring(o, t))) : "";
    }

    function getLogID() {
      var name = "BAIDUID";
      var u = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/~！@#￥%……&";
      var d = /[\uD800-\uDBFF][\uDC00-\uDFFFF]|[^\x00-\x7F]/g;
      var f = String.fromCharCode;

      function l(e) {
        if (e.length < 2) {
          var n = e.charCodeAt(0);
          return 128 > n ? e : 2048 > n ? f(192 | n >>> 6) + f(128 | 63 & n)
              : f(224 | n >>> 12 & 15) + f(128 | n >>> 6 & 63) + f(128 | 63
              & n);
        }
        var n = 65536 + 1024 * (e.charCodeAt(0) - 55296) + (e.charCodeAt(1)
            - 56320);
        return f(240 | n >>> 18 & 7) + f(128 | n >>> 12 & 63) + f(128 | n >>> 6
            & 63) + f(128 | 63 & n);
      }

      function g(e) {
        return (e + "" + Math.random()).replace(d, l);
      }

      function m(e) {
        var n = [0, 2, 1][e.length % 3];
        var t = e.charCodeAt(0) << 16 | (e.length > 1 ? e.charCodeAt(1) : 0)
            << 8 | (e.length > 2 ? e.charCodeAt(2) : 0);
        var o = [u.charAt(t >>> 18), u.charAt(t >>> 12 & 63),
          n >= 2 ? "=" : u.charAt(t >>> 6 & 63),
          n >= 1 ? "=" : u.charAt(63 & t)];
        return o.join("");
      }

      function h(e) {
        return e.replace(/[\s\S]{1,3}/g, m);
      }

      function p() {
        return h(g((new Date()).getTime()));
      }

      function w(e, n) {
        return n ? p(String(e)).replace(/[+\/]/g, function (e) {
          return "+" == e ? "-" : "_";
        }).replace(/=/g, "") : p(String(e));
      }

      return w(getCookie(name));
    }

    function Dialog() {
      var linkList = [];
      var showParams;
      var dialog, shadow;

      function createDialog() {
        var screenWidth = document.body.clientWidth;
        var dialogLeft = screenWidth > 800 ? (screenWidth - 800) / 2 : 0;
        var $dialog_div = $('<div class="dialog" style="width: 800px; top: 0px; bottom: auto; left: '
            + dialogLeft
            + 'px; right: auto; display: hidden; visibility: visible; z-index: 52;"></div>');
        var $dialog_header = $(
            '<div class="dialog-header"><h3><span class="dialog-title" style="display:inline-block;width:740px;white-space:nowrap;overflow-x:hidden;text-overflow:ellipsis"></span></h3></div>');
        var $dialog_control = $(
            '<div class="dialog-control"><span class="dialog-icon dialog-close">×</span></div>');
        var $dialog_body = $(
            '<div class="dialog-body" style="max-height:450px;overflow-y:auto;padding:0 20px;"></div>');
        var $dialog_tip = $(
            '<div class="dialog-tip" style="padding-left:20px;background-color:#faf2d3;border-top: 1px solid #c4dbfe;"><p></p></div>');

        $dialog_div.append($dialog_header.append($dialog_control)).append(
            $dialog_body);

        //var $dialog_textarea = $('<textarea class="dialog-textarea" style="display:none;width"></textarea>');
        var $dialog_radio_div = $(
            '<div class="dialog-radio" style="display:none;width:760px;padding-left:20px;padding-right:20px"></div>');
        var $dialog_radio_multi = $(
            '<input type="radio" name="showmode" checked="checked" value="multi"><span>多行</span>');
        var $dialog_radio_single = $(
            '<input type="radio" name="showmode" value="single"><span>单行</span>');
        $dialog_radio_div.append($dialog_radio_multi).append(
            $dialog_radio_single);
        $dialog_div.append($dialog_radio_div);
        $('input[type=radio][name=showmode]', $dialog_radio_div).change(
            function () {
              var value = this.value;
              var $textarea = $(
                  'div.dialog-body textarea[name=dialog-textarea]', dialog);
              var content = $textarea.val();
              if (value == 'multi') {
                content = content.replace(/\s+/g, '\n');
                $textarea.css('height', '300px');
              } else if (value == 'single') {
                content = content.replace(/\n+/g, ' ');
                $textarea.css('height', '');
              }
              $textarea.val(content);
            });

        var $dialog_button = $(
            '<div class="dialog-button" style="display:none"></div>');
        var $dialog_button_div = $(
            '<div style="display:table;margin:auto"></div>')
        var $dialog_copy_button = $(
            '<button id="dialog-copy-button" style="display:none">复制</button>');
        var $dialog_edit_button = $(
            '<button id="dialog-edit-button" style="display:none">编辑</button>');
        var $dialog_exit_button = $(
            '<button id="dialog-exit-button" style="display:none">退出</button>');

        $dialog_button_div.append($dialog_copy_button).append(
            $dialog_edit_button).append($dialog_exit_button);
        $dialog_button.append($dialog_button_div);
        $dialog_div.append($dialog_button);

        $dialog_copy_button.click(function () {
          var content = '';
          if (showParams.type == 'batch') {
            $.each(linkList, function (index, element) {
              if (element.downloadlink == 'error') {
                return;
              }
              if (index == linkList.length - 1) {
                content = content + element.downloadlink;
              } else {
                content = content + element.downloadlink + '\n';
              }
            });
          } else if (showParams.type == 'link') {
            $.each(linkList, function (index, element) {
              if (element.url == 'error') {
                return;
              }
              if (index == linkList.length - 1) {
                content = content + element.url;
              } else {
                content = content + element.url + '\n';
              }
            });
          }
          GM_setClipboard(content, 'text');
          alert('已将链接复制到剪贴板！');
        });

        $dialog_edit_button.click(function () {
          var $dialog_textarea = $(
              'div.dialog-body textarea[name=dialog-textarea]', dialog);
          var $dialog_item = $('div.dialog-body div', dialog);
          $dialog_item.hide();
          $dialog_copy_button.hide();
          $dialog_edit_button.hide();
          $dialog_textarea.show();
          $dialog_radio_div.show();
          $dialog_exit_button.show();
        });

        $dialog_exit_button.click(function () {
          var $dialog_textarea = $(
              'div.dialog-body textarea[name=dialog-textarea]', dialog);
          var $dialog_item = $('div.dialog-body div', dialog);
          $dialog_textarea.hide();
          $dialog_radio_div.hide();
          $dialog_item.show();
          $dialog_exit_button.hide();
          $dialog_copy_button.show();
          $dialog_edit_button.show();
        });

        $dialog_div.append($dialog_tip);
        $('body').append($dialog_div);
//        $dialog_div.dialogDrag();
        $dialog_control.click(dialogControl);
        return $dialog_div;
      }

      function createShadow() {
        var $shadow = $(
            '<div class="dialog-shadow" style="position: fixed; left: 0px; top: 0px; z-index: 50; background: rgb(0, 0, 0) none repeat scroll 0% 0%; opacity: 0.5; width: 100%; height: 100%; display: none;"></div>');
        $('body').append($shadow);
        return $shadow;
      }

      this.open = function (params) {
        showParams = params;
        linkList = [];
        if (params.type == 'link') {
          linkList = params.list.urls;
          $('div.dialog-header h3 span.dialog-title', dialog).text(params.title
              + "：" + params.list.filename);
          $.each(params.list.urls, function (index, element) {
            var $div = $('<div><div style="width:30px;float:left">'
                + element.rank
                + ':</div><div style="white-space:nowrap;overflow:hidden;text-overflow:ellipsis"><a href="'
                + element.url + '">' + element.url + '</a></div></div>');
            $('div.dialog-body', dialog).append($div);
          });
        } else if (params.type == 'batch') {
          linkList = params.list;
          $('div.dialog-header h3 span.dialog-title', dialog).text(
              params.title);
          if (params.showall) {
            $.each(params.list, function (index, element) {
              var $item_div = $(
                  '<div class="item-container" style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap"></div>');
              var $item_name = $('<div style="width:100px;float:left;overflow:hidden;text-overflow:ellipsis" title="'
                  + element.filename + '">' + element.filename + '</div>');
              var $item_sep = $(
                  '<div style="width:12px;float:left"><span>：</span></div>');
              var $item_link_div = $(
                  '<div class="item-link" style="float:left;width:618px;"></div>');
              var $item_first = $('<div class="item-first" style="overflow:hidden;text-overflow:ellipsis"><a href="'
                  + element.downloadlink + '">' + element.downloadlink
                  + '</a></div>');
              $item_link_div.append($item_first);
              $.each(params.alllist[index].links, function (n, item) {
                if (element.downloadlink == item.url) {
                  return;
                }
                var $item = $('<div class="item-ex" style="display:none;overflow:hidden;text-overflow:ellipsis"><a href="'
                    + item.url + '">' + item.url + '</a></div>');
                $item_link_div.append($item);
              });
              var $item_ex = $(
                  '<div style="width:15px;float:left;cursor:pointer;text-align:center;font-size:16px"><span>+</span></div>');
              $item_div.append($item_name).append($item_sep).append(
                  $item_link_div).append($item_ex);
              $item_ex.click(function () {
                var $parent = $(this).parent();
                $parent.toggleClass('showall');
                if ($parent.hasClass('showall')) {
                  $(this).text('-');
                  $('div.item-link div.item-ex', $parent).show();
                } else {
                  $(this).text('+');
                  $('div.item-link div.item-ex', $parent).hide();
                }
              });
              $('div.dialog-body', dialog).append($item_div);
            });
          } else {
            $.each(params.list, function (index, element) {
              var $div = $('<div style="overflow:hidden;text-overflow:ellipsis;white-space:nowrap"><div style="width:100px;float:left;overflow:hidden;text-overflow:ellipsis" title="'
                  + element.filename + '">' + element.filename
                  + '</div><span>：</span><a href="' + element.downloadlink
                  + '">' + element.downloadlink + '</a></div>');
              $('div.dialog-body', dialog).append($div);
            });
          }
        }

        if (params.tip) {
          $('div.dialog-tip p', dialog).text(params.tip);
        }

        if (params.showcopy) {
          $('div.dialog-button', dialog).show();
          $('div.dialog-button button#dialog-copy-button', dialog).show();
        }
        if (params.showedit) {
          $('div.dialog-button', dialog).show();
          $('div.dialog-button button#dialog-edit-button', dialog).show();
          var $dialog_textarea = $(
              '<textarea name="dialog-textarea" style="display:none;resize:none;width:758px;height:300px;white-space:pre;word-wrap:normal;overflow-x:scroll"></textarea>');
          var content = '';
          if (showParams.type == 'batch') {
            $.each(linkList, function (index, element) {
              if (element.downloadlink == 'error') {
                return;
              }
              if (index == linkList.length - 1) {
                content = content + element.downloadlink;
              } else {
                content = content + element.downloadlink + '\n';
              }
            });
          } else if (showParams.type == 'link') {
            $.each(linkList, function (index, element) {
              if (element.url == 'error') {
                return;
              }
              if (index == linkList.length - 1) {
                content = content + element.url;
              } else {
                content = content + element.url + '\n';
              }
            });
          }
          $dialog_textarea.val(content);
          $('div.dialog-body', dialog).append($dialog_textarea);
        }

        shadow.show();
        dialog.show();
      }

      this.close = function () {
        dialogControl();
      }

      function dialogControl() {
        $('div.dialog-body', dialog).children().remove();
        $('div.dialog-header h3 span.dialog-title', dialog).text('');
        $('div.dialog-tip p', dialog).text('');
        $('div.dialog-button', dialog).hide();
        $('div.dialog-radio input[type=radio][name=showmode][value=multi]',
            dialog).prop('checked', true);
        $('div.dialog-radio', dialog).hide();
        $('div.dialog-button button#dialog-copy-button', dialog).hide();
        $('div.dialog-button button#dialog-edit-button', dialog).hide();
        $('div.dialog-button button#dialog-exit-button', dialog).hide();
        dialog.hide();
        shadow.hide();
      }

      dialog = createDialog();
      shadow = createShadow();
    }

    function VCodeDialog(refreshVCode, confirmClick) {
      var dialog, shadow;

      function createDialog() {
        var screenWidth = document.body.clientWidth;
        var dialogLeft = screenWidth > 520 ? (screenWidth - 520) / 2 : 0;
        var $dialog_div = $('<div class="dialog" id="dialog-vcode" style="width:520px;top:0px;bottom:auto;left:'
            + dialogLeft
            + 'px;right:auto;display:none;visibility:visible;z-index:52"></div>');
        var $dialog_header = $(
            '<div class="dialog-header"><h3><span class="dialog-header-title"><em class="select-text">提示</em></span></h3></div>');
        var $dialog_control = $(
            '<div class="dialog-control"><span class="dialog-icon dialog-close icon icon-close"><span class="sicon">x</span></span></div>');
        var $dialog_body = $('<div class="dialog-body"></div>');
        var $dialog_body_div = $(
            '<div style="text-align:center;padding:22px"></div>');
        var $dialog_body_download_verify = $(
            '<div class="download-verify" style="margin-top:10px;padding:0 28px;text-align:left;font-size:12px;"></div>');
        var $dialog_verify_body = $('<div class="verify-body">请输入验证码：</div>');
        var $dialog_input = $(
            '<input id="dialog-input" type="text" style="padding:3px;width:85px;height:23px;border:1px solid #c6c6c6;background-color:white;vertical-align:middle;" class="input-code" maxlength="4">');
        var $dialog_img = $(
            '<img id="dialog-img" class="img-code" style="margin-left:10px;vertical-align:middle;" alt="点击换一张" src="" width="100" height="30">');
        var $dialog_refresh = $(
            '<a href="javascript:void(0)" style="text-decoration:underline;" class="underline">换一张</a>');
        var $dialog_err = $(
            '<div id="dialog-err" style="padding-left:84px;height:18px;color:#d80000" class="verify-error"></div>');
        var $dialog_footer = $('<div class="dialog-footer g-clearfix"></div>');
        var $dialog_confirm_button = $(
            '<a class="g-button g-button-blue" data-button-id="" data-button-index href="javascript:void(0)" style="padding-left:36px"><span class="g-button-right" style="padding-right:36px;"><span class="text" style="width:auto;">确定</span></span></a>');
        var $dialog_cancel_button = $(
            '<a class="g-button" data-button-id="" data-button-index href="javascript:void(0);" style="padding-left: 36px;"><span class="g-button-right" style="padding-right: 36px;"><span class="text" style="width: auto;">取消</span></span></a>');

        $dialog_header.append($dialog_control);
        $dialog_verify_body.append($dialog_input).append($dialog_img).append(
            $dialog_refresh);
        $dialog_body_download_verify.append($dialog_verify_body).append(
            $dialog_err);
        $dialog_body_div.append($dialog_body_download_verify);
        $dialog_body.append($dialog_body_div);
        $dialog_footer.append($dialog_confirm_button).append(
            $dialog_cancel_button);
        $dialog_div.append($dialog_header).append($dialog_body).append(
            $dialog_footer);
        $('body').append($dialog_div);

        //$dialog_div.dialogDrag();

        $dialog_control.click(dialogControl);
        $dialog_img.click(refreshVCode);
        $dialog_refresh.click(refreshVCode);
        $dialog_input.keypress(function (event) {
          if (event.which == 13) {
            confirmClick();
          }
        });
        $dialog_confirm_button.click(confirmClick);
        $dialog_cancel_button.click(dialogControl);
        $dialog_input.click(function () {
          $('#dialog-err').text('');
        });
        return $dialog_div;
      }

      this.open = function (vcode) {
        if (vcode) {
          $('#dialog-img').attr('src', vcode.img);
        }
        dialog.show();
        shadow.show();
      }
      this.close = function () {
        dialogControl();
      }
      dialog = createDialog();
      shadow = $('div.dialog-shadow');

      function dialogControl() {
        $('#dialog-img', dialog).attr('src', '');
        $('#dialog-err').text('');
        dialog.hide();
        shadow.hide();
      }
    }

    $.fn.dialogDrag = function () {
      var mouseInitX, mouseInitY, dialogInitX, dialogInitY;
      var screenWidth = document.body.clientWidth;
      var $parent = this;
      $('div.dialog-header', this).mousedown(function (event) {
        mouseInitX = parseInt(event.pageX);
        mouseInitY = parseInt(event.pageY);
        dialogInitX = parseInt($parent.css('left').replace('px', ''));
        dialogInitY = parseInt($parent.css('top').replace('px', ''));
        $(this).mousemove(function (event) {
          var tempX = dialogInitX + parseInt(event.pageX) - mouseInitX;
          var tempY = dialogInitY + parseInt(event.pageY) - mouseInitY;
          var width = parseInt($parent.css('width').replace('px', ''));
          tempX = tempX < 0 ? 0 : tempX > screenWidth - width ? screenWidth
              - width : tempX;
          tempY = tempY < 0 ? 0 : tempY;
          $parent.css('left', tempX + 'px').css('top', tempY + 'px');
        });
      });
      $('div.dialog-header', this).mouseup(function (event) {
        $(this).unbind('mousemove');
      });
    }

    (function () {
      var href = location.href;
      /http:/.test(href) ? location.href = 'https' + href.slice(4) : 0;
    }());

  })();
}, 200);