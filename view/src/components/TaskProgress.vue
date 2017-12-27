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
          case 'warn':
            ret = '#e1d04b';
            break;
          case 'exception':
            ret = '#ff4949';
            break;
          case 'busy':
            ret = '#FF8F43';
            break;
          default:
            ret = '#20a0ff';
        }
        return ret;
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
