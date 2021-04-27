<template>
  <Select
    v-model="selected"
    filterable
    allow-create
    @on-create="created"
    multiple
    @on-change="changed"
    :disabled="disabled"
    ref="select"
  >
    <Option disabled value="0">Please enter your state option if nothing is appropriate.</Option>
    <Option v-for="item in currentOptions" :value="item" :key="item">{{ item }}</Option>
  </Select>
</template>
<script>
export default {
  model: {
    prop: 'value',
    event: 'change'
  },
  props: ['value', 'canCreate', 'disabled'],
  mounted () {
    // 如果有原始赋值，添加到选项列表和已选列表中去
    this.currentOptions = [...this.value]
    this.selected = [...this.value]

    // 修改原始select创建item之前
    const originFuntion = this.$refs.select.handleCreateItem
    this.$refs.select.handleCreateItem = (hideMessage) => {
      let query = this.$refs.select.query
      let flag = true
      if (this.canCreate && query && this.selected.indexOf(query) < 0) {
        // 添加已选之前，先检查是否可以添加
        let {canCreate, message} = this.canCreate(query)
        flag = canCreate
        if (!flag && !hideMessage) {
          this.$Notice.warning({content: message})
        }
      }
      if (flag) { originFuntion() }
    }
    // 为input绑定事件，失焦时允许自动添加,但不满足添加条件的，不会显示message
    const input = this.$el.querySelector('input[type="text"]')
    input.addEventListener('blur', () => {
      this.$refs.select.handleCreateItem(true)
    })
  },
  watch: {
    value (newVal, oldVal) {
      // 如果值与原来一样，则不作修改
      if (JSON.stringify(newVal) === JSON.stringify(oldVal) &&
      JSON.stringify(newVal) === JSON.stringify(this.selected)) { return }
      // 如果出现了新值，需要再已有的option里添加选项
      newVal.map(val => {
        if (this.currentOptions.indexOf(val) < 0) {
          this.currentOptions.push(val)
        }
      })
      this.selected = [...newVal]
      // value改动，说明model的值改动
      this.$emit('on-change', newVal)
    }
  },
  data () {
    return {
      selected: [], // 已选
      currentOptions: []// 所有选项
    }
  },
  methods: {
    created (value) {
      this.$emit('optionCreated', value)
      // 在可选项中添加一项
      this.currentOptions.push(value)
    },
    changed () {
      this.$emit('change', this.selected)
    }
  }

}
</script>
