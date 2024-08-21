<template>
  <div class="login">
    <div class="login-content">
      <img class="login-left" src="~Assets/images/logo-login.png" />
      <div class="login-right">
        <div class="welcome-info">欢迎使用</div>
        <div class="login-form">
          <el-form ref="normalForm" :model="normalForm" :rules="normalFormRules" @keydown.enter.native="handleSubmitRegister">
            <el-form-item prop="mobile">
              <el-tooltip class="item" effect="dark" :content="userNametips" placement="left-start">
                <el-input type="text" v-model="normalForm.username" placeholder="请输入账号"> </el-input>
              </el-tooltip>
            </el-form-item>
            <el-form-item prop="password">
              <el-tooltip class="item" effect="dark" :content="passwordtips" placement="left-start">
                <el-input type="password" v-model="normalForm.password" placeholder="请输入登录密码" show-password> </el-input>
              </el-tooltip>
            </el-form-item>
            <el-form-item prop="passwordRepeat">
              <el-tooltip class="item" effect="dark" :content="passwordtips" placement="left-start">
                <el-input type="password" v-model="normalForm.passwordRepeat" placeholder="确认密码" show-password> </el-input>
              </el-tooltip>
            </el-form-item>
            <el-form-item prop="phone">
              <el-input type="text" v-model="normalForm.phone" placeholder="请输入电话号码"> </el-input>
            </el-form-item>
            <el-form-item prop="email">
              <el-input type="text" v-model="normalForm.email" placeholder="请输入邮箱地址"> </el-input>
            </el-form-item>
            <el-form-item>
              <el-button class="sub" style="width: 100%" @click="handleSubmitRegister" type="primary" long>注册</el-button>
            </el-form-item>
            <p class="tips" @click="goLogin">已有账号，去登录</p>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import { loginManageServer } from 'Api'
const sm2 = require('sm-crypto').sm2
export default {
  data() {
    return {
      normalForm: {
        username: '',
        password: '',
        passwordRepeat: '',
        phone: '',
        email: ''
      },
      userNametips: '用户名长度3～18个字符，支持数字、大小写字母、下划线_、连接符-',
      passwordtips: '密码长度8~18个字符，支持数字、大小写字母、特殊字符\'-!"#$%&()*,./:;?@[]^_`{|}~+<=>，至少包含一个数字和一个大写字母和一个小写字母和一个特殊字符',
      normalFormRules: {
        username: [{ validator: this.validUsername, trigger: 'blur' }],
        password: [{ validator: this.validPassword, trigger: 'blur' }],
        passwordRepeat: [{ validator: this.validatePasswordReapeat, trigger: 'blur' }],
        phone: [{ validator: this.validateMobile, trigger: 'blur' }],
        email: [{ required: true, message: '邮箱不能为空', trigger: 'blur' }]
      }
    }
  },
  computed: {
    ...mapGetters(['pbKey'])
  },
  components: {},
  methods: {
    encodePassword(password) {
      const { pbKey } = this
      const cipherMode = 1
      const encryptedPassword = sm2.doEncrypt(password, pbKey, cipherMode)
      return encryptedPassword
    },
    // 注册提交
    handleSubmitRegister() {
      this.$refs.normalForm.validate((valid) => {
        if (valid) {
          const { password = '', username = '', phone = '', email = '' } = this.normalForm
          const encodePassword = this.encodePassword(password)
          this.regNormal({ username, phone, email, password: encodePassword })
        }
      })
    },

    // 账号注册
    async regNormal(params) {
      const res = await loginManageServer.register(params)
      console.log(res)
      if (res.code === 0) {
        this.$message({ type: 'success', message: '注册成功!' })
        this.$router.push({ path: 'login' })
      }
    },
    goLogin() {
      this.$router.push('/login')
    },
    validateMobile(rule, value, callback) {
      if (!value) {
        return callback(new Error('手机号不能为空'))
      } else if (!/^1[3-9]\d{9}$/.test(value)) {
        return callback(new Error('手机号格式有误'))
      } else {
        callback()
      }
    },
    validatePasswordReapeat(rule, value, callback) {
      if (!value) {
        return callback(new Error('确认密码不能为空'))
      } else if (value !== this.normalForm.password) {
        return callback(new Error('两次输入的密码不一致'))
      } else {
        callback()
      }
    },
    validUsername(rule, str, callback) {
      if (!str) {
        return callback(new Error('用户名不能为空'))
      } else if (!/^[a-zA-Z0-9_-]{3,18}$/.test(str)) {
        return callback(new Error('用户名格式有误'))
      } else {
        callback()
      }
    },
    validPassword(rule, str, callback) {
      if (!str) {
        return callback(new Error('密码不能为空'))
      } else if (!/^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[\W])(?=.*[\S])^[0-9A-Za-z\S]{8,18}$/.test(str)) {
        return callback(new Error('密码格式有误'))
      } else {
        callback()
      }
    }
  }
}
</script>
<style lang="less" scoped>
.login {
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  min-width: 1280px;
  min-height: 720px;
  width: 100%;
  height: 100%;
  position: relative;
  box-sizing: border-box;
  background-image: url('~Assets/images/bg.png');
  background-size: 100% 100%;
  min-width: 1600px;
  .login-content {
    display: flex;
    align-items: center;
    padding: 0 180px 0 250px;
    justify-content: space-between;
    width: 100%;
  }
  .login-left {
    width: 45%;
    height: auto;
  }
  .login-right {
    width: 440px;
    // height: 496px;
    height: auto;
    padding: 60px 40px;
    border-radius: 12px;
    border: 1px solid #e0e4ed;
    box-shadow: 0px 4px 20px 2px #2e363f14;
    .welcome-info {
      color: #262a32;
      font-size: 24px;
    }
    .login-form {
      margin-top: 60px;
    }
    p.tips {
      font-size: 14px;
      line-height: 22px;
      margin-bottom: 16px;
      text-align: center;
      cursor: pointer;
      color: #3071f2;
      margin-top: 16px;
    }
    p.title {
      color: #787b84;
      margin-bottom: 8px;
      span {
        color: #3071f2;
        cursor: pointer;
      }
    }
  }
}
</style>
