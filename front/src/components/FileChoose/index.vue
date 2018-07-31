<template>
  <div style="display: inline">
    <Input class="file-choose-input"
      :value="value"
      readonly
      @dblclick.native="showChooser" />
    <Button type="primary"
      :disabled="disabled"
      @click="showChooser">选择</Button>
  </div>
</template>

<script>
import Chooser from "../../common/native";

export default {
  props: ["value", "mode", "disabled"],
  methods: {
    showChooser() {
      let chooserPromise;
      if (this.mode == "dir") {
        chooserPromise = Chooser.showDirChooser();
      } else {
        chooserPromise = Chooser.showFileChooser();
      }
      chooserPromise.then(result => {
        if (result) {
          this.$emit("input", result.path);
        }
      });
    }
  }
};
</script>

<style scoped>
.file-choose-input {
  width: 50%;
}
</style>
