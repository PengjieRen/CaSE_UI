<template>
  <Row :span="24" type="flex" justify="center" align="middle">
    <Col>
      <Card style="margin-top:150px;min-width:400px;text-align:center">
        <Form
          ref="form"
          :model="formInline"
          :rules="ruleInline"
          method="post"
          :disabled="submitting"
          @submit.native.prevent
        >
          <h1>Your session has expired!</h1>
          <Divider />
          <FormItem prop="user">
            <Input type="text" v-model="formInline.user" placeholder="Username" name="name">
              <Icon type="ios-person-outline" slot="prepend"></Icon>
            </Input>
          </FormItem>
           <FormItem prop="password">
            <Input type="password" v-model="formInline.password" placeholder="Password">
                <Icon type="ios-lock-outline" slot="prepend"></Icon>
            </Input>
        </FormItem>
          <FormItem>
            <Button type="primary" @click="handleSubmit('form')">Login</Button>
          </FormItem>
        </Form>
      </Card>
    </Col>
  </Row>
</template>
<script>
export default {
  data () {
    return {
      formInline: {
        user: '',
        password: ''
      },
      submitting: false,
      ruleInline: {
        user: [
          { required: true, message: 'Please fill in the user name', trigger: 'blur' }
        ],
        password: [
          { required: true, message: 'Please fill in the password.', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    handleSubmit (name) {
      this.$refs[name].validate((valid) => {
        if (valid) {
          this.submitting = true
          this.$http.post('/checkLogin',
            'username=' + this.formInline.user + '&password=' + this.formInline.password,
            {
              headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
              }
            }
          )
            .then((response) => {
              if (response.data.code === 200) {
                window.location = response.data.profile ? '/index?_uuid=' + response.data.uuid + '&_user=' + this.formInline.user : '/profile?_uuid=' + response.data.uuid + '&_user=' + this.formInline.user
              } else {
                this.$Message.error({
                  content: response.data.msg,
                  duration: 10,
                  closable: true
                })
                this.submitting = false
              }
            }).catch((e) => {
              this.submitting = false
              this.$Message.error('Something wrong when sending data!')
            })
        }
      })
    }
  }
}
</script>
