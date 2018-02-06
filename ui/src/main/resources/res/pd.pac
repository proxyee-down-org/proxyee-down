function FindProxyForURL(url, host) {
  var regs = [
    '^https://(pan|yun).baidu.com.*$',
    '^https://.*.baidupcs.com/file/.*$',
    '^https://.*.baidupcs.com/rest/.*/pcs/file.*$',
  ];
  var match = false;
  for (var i = 0; i < regs.length; i++) {
    if (url.match(regs[i])) {
      match = true;
      break;
    }
  }
  return match ? "PROXY 127.0.0.1:{port}" : "DIRECT";
}