<script>
import cascader from 'view-design/src/components/cascader/cascader'
import customCascaderPanel from './customCascaderPanel'

const mixin = {
  components: { Caspanel: customCascaderPanel }
}

export default {
  mixins: [mixin],
  extends: cascader
}
</script>
