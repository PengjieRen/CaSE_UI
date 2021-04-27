<template>
  <li :class="classes" style="padding-top:10px">
    <Render v-if="data.render" :data="data" :render="data.render" />
    <Render v-else :render="rendItem" :data="data" />
    <Icon type="ios-arrow-forward" v-if="showArrow" />
    <i
      v-if="showLoading"
      class="ivu-icon ivu-icon-ios-loading ivu-load-loop ivu-cascader-menu-item-loading"
    ></i>
  </li>
</template>
<script>
import Render from './render'
import helpTooltip from './helpTooltip'
export default {
  components: {Render},
  props: {
    data: Object,
    prefixCls: String,
    tmpItem: Object
  },
  created () {
    if (!this.data.value) {
      this.data.value = this.data.name
    }
    if (!this.data.label) {
      this.data.label = this.data.title
    }
  },
  computed: {
    classes () {
      return [
        `${this.prefixCls}-menu-item`,
        {
          [`${this.prefixCls}-menu-item-active`]: this.tmpItem.value === this.data.value,
          [`${this.prefixCls}-menu-item-disabled`]: this.data.disabled
        }
      ]
    },
    showArrow () {
      return (this.data.children && this.data.children.length) || ('loading' in this.data && !this.data.loading)
    },
    showLoading () {
      return 'loading' in this.data && this.data.loading
    }
  },
  methods: {
    rendItem (h, data) {
      return h('div', [
        data.title,
        data.tooltip && h(helpTooltip, {
          props: {
            content: data.tooltip
          },
          on: {
            showTooltip: () => {
              if (this.$parent) {
                this.$parent.$emit('showTooltip', data)
              }
            }
          }
        })
      ])
    }
  }
}
</script>
