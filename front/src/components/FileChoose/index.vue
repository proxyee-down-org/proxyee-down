<template>
  <div class="file-choose">
    <Input class="file-choose-input"
      :value="value"
      readonly
      disabled />
    <Button type="primary"
      class="file-choose-button"
      :disabled="disabled||chooserWait"
      @click="showChooser">{{$t('tip.choose')}}</Button>
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
.file-choose {
  display: inline-block;
  width: 100%;
}
.file-choose-input {
  width: 85%;
  padding-right: 3px;
}
.file-choose-button {
  width: 15%;
}
</style>
