<style scoped>
.message-body {
  position: relative;
  min-width: 150px;
  min-height: 35px;
  max-width: 75%;
  border-radius: 5px;
  background: #fff;
  margin: 15px 5px auto;
  text-align: left;
  word-wrap: break-word;
  word-break: break-word;
  padding: 5px 10px;
  border: 1px solid #dcdee2;
  border-color: #e8eaec;
}
.message-body .arrow {
  position: absolute;
  top: 3px;
  right: -10px;
  width: 0;
  height: 0;
  font-size: 0;
  border: solid 6px;
  border-color: transparent transparent transparent #fff;
}
.message-container .time {
  position: absolute;
  top: 6px;
  right: 44px;
  left: 0;
  font-size: 10px;
  color: darkgray;
  text-align: right;
}
.message-container .time.left {
  text-align: left;
  left: 44px;
  right: 0;
}
.message-body .arrow.left {
  left: -10px;
  border-color: transparent #fff transparent transparent;
}
.message-body.color {
  background: #2b85e4;
  color: white;
}
.message-body.color .arrow {
  border-color: transparent transparent transparent #2b85e4;
}

.message-container {
  position: relative;
  padding: 5px;
}
.avatar {
  float: left;
}
</style>

<template>
  <div class="message-container">
    <div class="time" :class="{left:data.position==='left'}">{{formatTime(data.time)}}</div>
    <div class="avatar" :style="{float:data.position}">
      <Avatar
        :style="{
        background: data.color==true? 'red':data.color||'#f56a00'}"
      >{{ data.user }}</Avatar>
    </div>
    <div class="message-body" :class="{color:data.color===true}" :style="{float:data.position}">
      <div class="arrow" :class="{left:data.position==='left'}"></div>
      <p v-html="data.message"></p>
    </div>
    <div class="clearfix"></div>
  </div>
</template>
<script>

export default {
  props: {
    data: {
      type: Object
    }
  },
  methods: {
    formatTime (time) {
      if (time instanceof Date) {
        return this.prefixInteger(time.getFullYear(), 4) + '-' +
          this.prefixInteger((time.getMonth() + 1), 2) + '-' +
          this.prefixInteger(time.getDate(), 2) + ' ' +
         this.prefixInteger(time.getHours(), 2) + ':' +
         this.prefixInteger(time.getMinutes(), 2) + ':' +
          this.prefixInteger(time.getSeconds(), 2)
      }
      return time + ''
    },
    prefixInteger (num, length) {
      return (Array(length).join('0') + num).slice(-length)
    }
  }
}
</script>
