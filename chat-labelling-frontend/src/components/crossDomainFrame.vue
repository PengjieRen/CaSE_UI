<template>
  <div ref="a">
    <iframe ref="frame" :src="src" name="abc" />
  </div>
</template>
<script>
export default {
  props: ['src'],
  watch: {
    src (newVal) {
    }
  },
  mounted () {
    this.$refs.frame.onload = () => {
      console.log(this.$refs.frame.contentWindow)
    }
  },
  methods: {
    initFrame () {
    },
    iframeLoadedMessage (doc) {
      console.log(doc)
    },
    iframeOnReadyStateChangeMessage () {

    }
  }
}
</script>
