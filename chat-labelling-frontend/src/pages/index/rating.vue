<template>
  <div style="width:800px; margin: 100px auto">
    <h1 style="text-align:center">Rate this conversation</h1>
    <Divider />
    <Form :model="formItem" ref="form" label-position="top" :rules="rules" :show-message="false">
      <FormItem prop="goalAchieve" v-if="role==='cus'">
        <h3 slot="label">Did you achieve your goal?</h3>
        <RadioGroup v-model="formItem.goalAchieve">
          <Radio label="yes">Yes</Radio>
          <Radio label="no">No</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="goalUnderstand" v-if="role==='sys'">
        <h3 slot="label">Do you think you understand user's goal?</h3>
        <RadioGroup v-model="formItem.goalUnderstand">
          <Radio label="yes">Yes</Radio>
          <Radio label="no">No</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="conversationSearch" v-if="role==='cus'">
        <h3 slot="label">Would you like to conduct search through conversations in this way?</h3>
        <RadioGroup v-model="formItem.conversationSearch">
          <Radio label="yes">Yes</Radio>
          <Radio label="no">No</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="easierWay" v-if="role==='sys'">
        <h3 slot="label">Which one is easier for user to achieve his/her goal?</h3>
        <RadioGroup v-model="formItem.easierWay">
          <Radio label="search">Search</Radio>
          <Radio label="conversation">Conversation</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="rate">
        <h3 slot="label">What do you think of your partner's performance?</h3>
        <Rate allow-half v-model="formItem.rate" />
      </FormItem>
      <FormItem>
        <Button type="primary" @click="restart">Submit</Button>
        <!-- <Button @click="logout">Submit and Logout</Button> -->
        <!-- <Button @click="close">Submit and Close</Button> -->
      </FormItem>
    </Form>
  </div>
</template>
<script>
export default {
  props: ['role', 'conversationId'],
  data () {
    return {
      formItem: {
        goalAchieve: '',
        conversationSearch: '',
        rate: 0,
        goalUnderstand: '',
        easierWay: ''
      },
      rules: {
        goalAchieve: [ { trigger: 'change' } ],
        conversationSearch: [ { message: 'Please select ', trigger: 'change' } ],
        rate: [ {trigger: 'change',
          validator: (rule, value, callback) => {
            if (!value) {
              return callback(new Error('Please rate your partner!'))
            } else callback()
          }} ],
        goalUnderstand: [ { trigger: 'change' } ],
        easierWay: [ { trigger: 'change' } ]
      }
    }
  },
  methods: {
    restart () {
      this.$refs.form.validate((valid) => {
        if (!valid) {
          this.$Message.error('Please fill in the form!')
          return
        }
        this.$http.post('/api/saveRating', {...this.formItem, role: this.role, conversationId: this.conversationId}).then((response) => {
          this.$emit('completed')
        }).catch((e) => {
          this.submitting = false
          this.$Message.error('Something wrong when sending data!')
        })
      })
    },
    logout () {
      this.$refs.form.validate((valid) => {
        if (!valid) {
          this.$Message.error('Please fill in the form!')
          return
        }
        this.$http.post('/api/saveRating', {...this.formItem, role: this.role, conversationId: this.conversationId}).then((response) => {
          window.location = '/logout'
        }).catch((e) => {
          this.submitting = false
          this.$Message.error('Something wrong when sending data!')
        })
      })
    }
  }
}
</script>
