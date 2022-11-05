<template>
  <div class="login" style="text-align: center">
    <div class="tit">老实人PUSH系统</div>
    <el-form class="form" ref="form" :model="form" @submit.native.prevent="onSubmit">
      <el-form-item class="center">
        <el-input v-model="form.name" placeholder="用户名"></el-input>
      </el-form-item>
      <el-form-item class="center">
        <el-button type="success" @click="onSubmit">卷死别人</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import request from "@/utils/request";

export default {
  name: 'LoginView',
  data() {
    return {
      logined: false,
      form: {
        name: '',
      }
    }
  },
  methods:{
    onSubmit() {
      // 登陆成功
      request.post("/api/login", this.form.name).then(res => {
        if (res.code === '0') {
          localStorage.setItem("username", this.form.name)
          this.$router.push('/collect-dialog-corpus')
        }
      })
    }
  }
}
</script>

<style lang = "scss" scoped>
.login{
  max-width: 300px;
  border-radius: 5px;
  padding: 10px 40px 30px;
  margin: 60px auto 0;
  background-color:#efefee;
  .tit{
    font-size: 26px;
    text-align: center;
    line-height: 26px;
    padding: 30px 0;
  }
  .center{
    display: flex;
    justify-content: center;
  }
}
</style>