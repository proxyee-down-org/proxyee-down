<template>
  <a @click.prevent="onClick"><slot></slot></a>
</template>

<script>
  import Vue from 'vue'

  export default {
    methods: {
      onClick() {
        if (jNative) {
          jNative.open(this.$attrs.href);
        } else {
          open(this.$attrs.href);
        }
      }
    }
  }
</script>
