<script>
  import ElementUI from 'element-ui'

  export default {
    extends: ElementUI.Progress,
    computed: {
      stroke() {
        var ret;
        switch (this.status) {
          case 'success':
            ret = '#13ce66';
            break;
          case 'ready':
            ret = '#e1d04b';
            break;
          case 'exception':
            ret = '#ff4949';
            break;
          case 'pause':
            ret = '#6C6C6C';
            break;
          case 'merge':
            ret = '#d497ee';
            break;
          default:
            ret = '#20a0ff';
        }
        return ret;
      },
      barStyle() {
        var style = {};
        style.width = this.percentage + '%';
        style.backgroundColor = this.stroke;
        return style;
      },
      iconClass() {
        if (this.type === 'line') {
          return this.status === 'success' ? 'el-icon-circle-check' : 'el-icon-circle-cross';
        } else {
          switch (this.status){
            case 'success':
              return 'el-icon-check';
            case 'warn':
            case 'busy':
              return '';
            default:
              return 'el-icon-close';
          }
        }
      },
    },
  }
</script>
