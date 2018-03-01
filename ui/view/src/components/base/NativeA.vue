<template>
  <a @click.prevent="onClick">
    <slot></slot>
  </a>
</template>

<script>
  export default {
    methods: {
      onClick() {
        this.$http.get('api/open?url='+this.$attrs.href).then(result => {
          console.log(result);
          if (result.data == 2) {
            window.open(this.$attrs.href);
          }
        }).catch(() => {
        });
      }
    }
  }
</script>
