<template>
  <Tooltip :content="content" transfer @on-popper-show="showTooltip" :max-width="200">
    <Icon type="md-help-circle" style="position:relative;right:0" />
  </Tooltip>
</template>
<script>
export default {
  props: ['content'],
  methods: {
    showTooltip () {
      this.$emit('showTooltip')
    }
  }
}
</script>
