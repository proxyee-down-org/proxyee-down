<template>
  <div style="display: inline">
    <Input class="file-choose-input"
      :value="value"
      readonly
      @dblclick.native="showChooser" />
    <Button type="primary"
      :disabled="disabled||chooserWait"
      @click="showChooser">选择</Button>
  </div>
</template>

<script>
import { showDirChooser, showFileChooser } from '../../common/native'

export default {
  props: {
    value: {
      type: String
    },
    mode: {
      type: String,
      default: 'dir'
    },
    disabled: {
      type: Boolean
    }
  },
  data() {
    return {
      chooserWait: false
    }
  },
  methods: {
    showChooser() {
      this.chooserWait = true
      let chooserPromise =
        this.mode === 'dir' ? showDirChooser() : showFileChooser()
      chooserPromise
        .then(result => {
          if (result) {
            this.$emit('input', result.path)
          }
        })
        .finally(() => {
          this.chooserWait = false
        })
    }
  }
}
</script>

<style scoped>
.file-choose-input {
  width: 80%;
  padding-right: 3px;
}
</style>
