export default {
  sizeFmt(size, def) {
    if (size <= 0) {
      if (def) {
        return def;
      } else {
        size = 0;
      }
    }
    let unit = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
    let pow = 0;
    let temp = size;
    while (temp / 1000 >= 1) {
      pow++;
      temp /= 1000;
    }
    let fmt = unit[pow];
    return parseFloat((size / Math.pow(1024, pow)).toFixed(2)) + fmt;
  },
  timeFmt(sec) {
    if (sec < 0) {
      sec = 1;
    }
    let ret = '';
    sec = Math.ceil(sec);
    if (sec / 3600 >= 1) {
      ret += parseInt(sec / 3600) + '时';
    }
    if (ret.length > 0 || sec % 3600 / 60 >= 1) {
      ret += parseInt(sec % 3600 / 60) + '分';
    }
    ret += sec % 60 + '秒';
    return ret;
  },
  uuid() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g,
      function (c) {
        var r = (d + Math.random() * 16) % 16 | 0;
        d = Math.floor(d / 16);
        return (c == 'x' ? r : (r & 0x3 | 0x8)).toString(16);
      });
    return uuid;
  },
  inArray(array, obj, equals) {
    if (array) {
      for (let i = 0; i < array.length; i++) {
        if (equals) {
          if (equals(array[i], obj)) {
            return i;
          }
        } else if (array[i] == obj) {
          return i;
        }
      }
    }
    return -1;
  },
  clone(obj, ignores) {
    let newObj = {};
    for (let name in obj) {
      if (!ignores || this.inArray(ignores, name) == -1) {
        newObj[name] = obj[name];
      }
    }
    return newObj;
  },
  copy(fromObj, toObj, ignores) {
    for (let name in fromObj) {
      if (!ignores || this.inArray(ignores, name) == -1) {
        toObj[name] = fromObj[name];
      }
    }
  },
  getFileNameNoSuffix(fileName) {
    let index = fileName.lastIndexOf(".");
    if (index != -1) {
      return fileName.substring(0, index);
    } else {
      return fileName + "_unzip"
    }
  },
  getUnzipFilePath(filePath, fileName) {
    if (!filePath) {
      return '';
    }
    if (filePath.lastIndexOf('\\') == filePath.length - 1) {
      return filePath + this.getFileNameNoSuffix(fileName);
    } else {
      return filePath + '\\' + this.getFileNameNoSuffix(fileName);
    }
  },
}
