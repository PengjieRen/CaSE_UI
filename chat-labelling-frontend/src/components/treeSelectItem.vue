<template>
  <span>
    <span v-if="data.children">
      {{data.title}}
      <helpTooltip :content="data.tooltip" @showTooltip="showTooltip" />
    </span>
    <Option v-else :value="data.id" style="margin:-2px" :tag="data.tag">
      <Checkbox :value="selected"></Checkbox>
      {{data.title}}
      <helpTooltip :content="data.tooltip" @showTooltip="showTooltip" />
    </Option>
  </span>
</template>
<script>
import helpTooltip from './helpTooltip'
export default {
  components: {helpTooltip},
  props: ['data', 'selected'],
  methods: {
    showTooltip () {
      this.$emit('showNodeTooltip', this.data)
    }
  }
}
</script>
