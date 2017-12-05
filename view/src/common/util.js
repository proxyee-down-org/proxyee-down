export default {
  sizeFmt(size) {
    let unit = ['B', 'KB', 'MB', 'GB', 'TB', 'PB'];
    let pow = 0;
    let temp = size;
    while (temp / 1000 >= 1) {
      pow++;
      temp /= 1000;
    }
    let fmt = unit[pow];
    return parseFloat((size / Math.pow(1024, pow)).toFixed(2)) + fmt;
  }
}
