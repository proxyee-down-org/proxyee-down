<template>
  <a @click.prevent="onClick"><slot></slot></a>
</template>

<script>
  import Vue from 'vue'

  export default {
    props: ['text'],
    methods: {
      onClick() {
        if (window.jNative) {
          window.jNative.open(this.$attrs.href);
        } else {
          window.open(this.$attrs.href);
        }
      }
    }
  }
</script>
