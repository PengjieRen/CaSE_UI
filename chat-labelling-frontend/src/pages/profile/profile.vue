<template>
  <div style="width:800px;margin:100px auto;">
    <h1 style="text-align:center">Please fill in your information:</h1>
    <Divider />
    <Form :model="formItem" :rules="rules" ref="form" :disabled="submitting" label-position="top">
      <FormItem prop="gender">
        <strong slot="label">Gender</strong>
        <RadioGroup v-model="formItem.gender">
          <Radio label="male">Male</Radio>
          <Radio label="female">Female</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="age">
        <strong slot="label">Age</strong>
        <RadioGroup v-model="formItem.age">
          <Radio label="0-13">0-13</Radio>
          <Radio label="14-25">14-25</Radio>
          <Radio label="25-35">25-35</Radio>
          <Radio label="35-45">35-45</Radio>
          <Radio label="45-60">45-60</Radio>
          <Radio label=">=60">>=60</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem prop="majors">
        <strong slot="label">Majors</strong>
        <RadioGroup v-model="formItem.majors">
          <Radio label="IR">Information retrieval related</Radio>
          <Radio label="CS other than IR">Computer science other than information retrieval</Radio>
          <Radio label="Other">Other</Radio>
        </RadioGroup>
      </FormItem>
      <FormItem>
        <Button type="primary" @click="submit">Submit</Button>
        <!-- <Button type="primary" @click="submit">Submit and login with new account</Button> -->
        <!-- <Button @click="login">Login with Existing account>></Button> -->
      </FormItem>
    </Form>
  </div>
</template>
<script>
export default {
  data () {
    return {
      formItem: {
        gender: '',
        age: '',
        majors: ''
      },
      submitting: false,
      rules: {
        gender: [
          { required: true, message: 'Please select gender', trigger: 'change' }
        ],
        age: [
          { required: true, message: 'Please select age', trigger: 'change' }
        ],
        majors: [
          {required: true, message: 'Please select major', trigger: 'change'}
        ]
      }
    }
  },
  methods: {
    submit () {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.submitting = true
          // this.$http.post('/api/saveProfileAndLogin', this.formItem).then((response) => {
          this.$http.post('/api/saveProfile', this.formItem).then((response) => {
            window.location = '/index?' + window.location.href.split('?')[1]
          }).catch((e) => {
            this.submitting = false
            this.$Message.error('Something wrong when sending data!')
          })
        }
      })
    },
    login () {
      window.location = '/login'
    }
  }
}
</script>
