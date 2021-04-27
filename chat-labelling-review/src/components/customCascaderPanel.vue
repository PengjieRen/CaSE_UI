<script>
import customCasitem from './customCasitem'
import panel from 'view-design/src/components/cascader/caspanel'
const mixin = {
  components: {
    Casitem: customCasitem
  }
}
export default {
  name: 'Caspanel',
  mixins: [mixin],
  extends: panel,
  created () {
    this.$on('showTooltip', (data) => {
      // dispatch是iview内部方法，继承组件可以使用，用dispatch直接将事件传到级联组件的根节点上
      this.dispatch('Cascader', 'showTooltip', data)
    })
  }
}
</script>
