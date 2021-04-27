<style scoped>
.selected-list {
  min-height: 100px;
}
.list-item-content {
  font-size: 60%;
  color: gray;
}
.empty-list {
  background: #dcdee2;
  text-align: center;
  font-size: 150%;
  font-weight: bold;
  padding-top: 30px;
  min-height: 100px;
}
.deletable-list-item {
  position: absolute;
  top: 0;
  right: 0;
}
.flip-list-move {
  transition: transform 0.5s;
}
.no-move {
  transition: transform 0s;
}
.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
.list-group {
  min-height: 20px;
  list-style-type: none;
}
.list-group-item {
  cursor: move;
  border: 1px solid rgba(0, 0, 0, 0.125);
  padding: 0.2rem 1.25rem;
  margin-bottom: -1px;
  position: relative;
}
.list-group-item p {
  line-height: 100%;
}
</style>
<template>
  <div class="selected-list">
    <div v-if=" listItems.length===0" class="empty-list">
      <span>Nothing selected</span>
    </div>
    <draggable v-else class="list-group" tag="ul" v-model="listItems" @end="dragEnd">
      <transition-group type="transition" name="flip-list">
        <li class="list-group-item" v-for="(element,index) in listItems" :key="element.id">
          {{ element.title }}
          <a v-if="element.link" target="_blank" :href=element.link>{{element.link}}</a>
<!--          <p v-if="element.content">-->
<!--            <small class="list-item-content">{{element.content}}</small>-->
<!--          </p>-->
          <div class="deletable-list-item">
            <Button type="text" icon="md-close-circle" @click="deleteItem(element,index)"></Button>
          </div>
        </li>
      </transition-group>
    </draggable>
  </div>
</template>
<script>
import draggable from 'vuedraggable'

export default {
  model: {
    prop: 'value',
    event: 'change'
  },
  components: {draggable},
  props: ['value'],
  created () {
    if (this.value.length > 0) {
      this.listItems = [...this.value]
    }
  },
  watch: {
    value (newVal, oldVal) {
      this.listItems = [...newVal]
      // value改动，说明model的值改动
      this.$emit('on-change', newVal)
    }
  },
  data () {
    return {
      listItems: []
    }
  },
  methods: {
    deleteItem (element, index) {
      this.listItems.splice(index, 1)
      this.$emit('change', this.listItems)
    },
    dragEnd () {
      this.$emit('change', this.listItems)
    }
  }

}
</script>
