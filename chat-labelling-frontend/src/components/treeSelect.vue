<template>
  <div>
    {{selected}}
    <Select
      v-model="selected"
      multiple
      not-found-text="No availabel data"
      @on-change="selectChanged"
      @on-open-change="openTreeSelect"
    >
      <Tree
        v-if="data.length>0"
        :data="data"
        :render="renderContent"
        style="padding:0 20px"
        @on-toggle-expand="toggleExpand"
      ></Tree>
    </Select>
  </div>
</template>
<script>
import selectItem from './treeSelectItem'
export default {
  model: {
    prop: 'value',
    event: 'change'
  },
  data () {
    return {
      selected: []
    }
  },
  created () {
    if (this.value.length > 0) {
      this.selected = [...this.value]
    }
  },
  watch: {
    value (newVal, oldVal) {
      this.selected = [...newVal]
      // value改动，说明model的值改动
      this.$emit('on-change', newVal)
    }
  },
  props: {
    data: {
      type: Array,
      default: () => {
        return []
      }
    },
    value: {
      type: Array,
      default: () => {
        return []
      }
    }
  },
  methods: {
    showNodeTooltip (treeNode) {
      this.$emit('showNodeTooltip', treeNode)
    },
    openTreeSelect (opened) {
      this.$emit('openTreeSelect', opened)
    },
    toggleExpand (node) {
      this.$emit('toggleExpand', node)
    },
    renderContent (h, { root, node, data }) {
      return h(selectItem, {
        props: {
          data,
          selected: this.selected.indexOf(data.id) >= 0
        },
        on: {
          showNodeTooltip: this.showNodeTooltip
        }
      })
    },
    selectChanged () {
      this.$emit('change', this.selected)
    }
  }
}
</script>
