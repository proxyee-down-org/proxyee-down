export default {
  sizeFmt(size) {
    if(size<=0){
      return "未知大小";
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
}
