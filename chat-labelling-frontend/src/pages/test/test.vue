<template>
  <div style="width:700px;float:right">
    <labelling
      :height="height"
      ref="labelling"
      :role="'sys'"
      :currentState="currentState"
      :searchData="searchData"
      :sendData="sendData"
      :sendDisabled="disabled"
      :actions="actions"
    />
  </div>
  <!-- <div>
    <Button @click="value5 = true" type="primary">Open Drawer</Button>
    <Drawer
      title="Multi-level drawer"
      :width="45"
      :closable="true"
      v-model="value5"
      scrollable
      draggable
      placement="left"
      :mask="false"
      :mask-closable="false"
    >
      <div>
        <Button @click="value6 = true" type="primary">Two-level Drawer</Button>
        <Drawer
          title="Two-level Drawer"
          :closable="true"
          v-model="value6"
          placement="left"
          draggable
          scrollable
          :mask="false"
          :mask-closable="false"
          style="z-index:10"
        >This is two-level drawer.</Drawer>
      </div>
    </Drawer>
  </div>-->
  <!-- <rating role="sys" /> -->
  <!-- <profile role="cus" /> -->
  <!-- <div>
    <createSelect v-model="selectedItems" />
    <Button @click="searchData(selectedItems,()=>{})">search</Button>
  </div> -->
</template>
<script>
import data from './data'
import customCascader from '../../components/customCascader'
import createSelect from '../../components/createSelect'
import crossDomainFrame from '../../components/crossDomainFrame'

import labelling from '../index/labelling'
import rating from '../index/rating'
import profile from '../profile/profile.vue'

export default {
  components: {
    customCascader,
    createSelect,
    labelling,
    profile,
    rating,
    crossDomainFrame
  },
  mounted () {
  },
  destroyed () {
    window.removeEventListener('beforeunload', e => this.beforeunloadFn(e))
  },
  data () {
    return {
      disabled: false,
      actions: data.actionData,
      height: '400px',
      currentState: [],
      value5: false,
      value6: false,
      selectedItems: []
    }
  },
  methods: {
    searchData (states, callback) {
      if (states.length === 0) {
        return
      }
      this.$jsonp('http://localhost:9191?query=' + encodeURIComponent(states.join(' ')), {
        callbackQuery: 'callback',
        callbackName: 'searchDataCallback'
      }).then((json) => {
        console.log('~~~', json)
      })
    },
    copyLink (link) {
      this.$copyText(link).then((e) => {
        this.$Message.info('Copied!')
      }, (e) => {
        this.$Message.info('Can not copy!')
      })
    },
    beforeunloadFn (e) {
      e = e || window.event
      if (e) {
        e.returnValue = '关闭提示'
      }
      return '关闭提示'
    },
    sendData (data, callback) {
      console.log('sendData')
      this.disabled = true
      callback()
    },
    loadState () {
      this.currentState = ['c', 'd']
    }
  }

}
</script>
