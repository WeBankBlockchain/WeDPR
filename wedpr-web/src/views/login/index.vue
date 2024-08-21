<template>
  <div class="login">
    <div class="login-content">
      <img class="login-left" src="~Assets/images/logo-login.png" />
      <div class="login-right">
        <div class="welcome-info">欢迎使用</div>
        <div class="login-form">
          <el-form ref="normalForm" :model="normalForm" :rules="normalFormRules" @keydown.enter.native="handleSubmit">
            <el-form-item prop="mobile">
              <el-input type="text" v-model="normalForm.username" placeholder="请输入账号"> </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input type="password" v-model="normalForm.password" placeholder="请输入登录密码" show-password> </el-input>
            </el-form-item>
            <el-form-item prop="imageCode">
              <el-input style="width: 244px" type="input" v-model="normalForm.imageCode" placeholder="请输入验证码"> </el-input>
              <img class="code" @click="getImageCode" :src="imageBase64" alt="" />
            </el-form-item>
            <el-form-item>
              <el-button class="sub" style="width: 100%" @click="handleSubmit" type="primary" long>登录</el-button>
            </el-form-item>
            <p class="tips" @click="goRegister">没有账号，去注册</p>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapMutations, mapGetters } from 'vuex'
import {
  SET_GROUPLIST,
  SET_ALGLIST,
  SET_AGENCYLIST,
  SET_AUTHORIZATION,
  SET_PERMISSION,
  SET_USERINFO,
  SET_USERID,
  SET_AGENCYID,
  SET_AGENCYNAME,
  SET_AGENCYADMIN
} from 'Store/mutation-types.js'
import { loginManageServer, settingManageServer, accountManageServer } from 'Api'
import { permissionMap } from 'Utils/config.js'
import { algListFull } from 'Utils/constant.js'
import { jwtDecode } from 'jwt-decode'
const sm2 = require('sm-crypto').sm2
export default {
  data() {
    return {
      normalForm: {
        username: '',
        password: '',
        imageCode: ''
      },
      imageBase64: '',
      randomToken: '',
      normalFormRules: {
        username: [{ required: true, message: '账号不能为空', trigger: 'blur' }],
        password: [{ required: true, message: '密码不能为空', trigger: 'blur' }],
        imageCode: [{ required: true, message: '验证码不能为空', trigger: 'blur' }]
      }
    }
  },
  components: {},
  created() {
    this.getImageCode()
  },
  computed: {
    ...mapGetters(['pbKey'])
  },
  methods: {
    ...mapMutations([SET_GROUPLIST, SET_ALGLIST, SET_AGENCYLIST, SET_AUTHORIZATION, SET_PERMISSION, SET_USERINFO, SET_USERID, SET_AGENCYID, SET_AGENCYNAME, SET_AGENCYADMIN]),
    encodePassword(password) {
      const { pbKey } = this
      const cipherMode = 1
      const encryptedPassword = sm2.doEncrypt(password, pbKey, cipherMode)
      return encryptedPassword
    },
    async getImageCode() {
      const res = await settingManageServer.getImageCode()
      if (res.code === 0 && res.data) {
        const { imageBase64, randomToken } = res.data
        this.randomToken = randomToken
        this.imageBase64 = `data:image/png;base64,${imageBase64}`
      }
    },

    // 登录提交
    handleSubmit() {
      this.$refs.normalForm.validate((valid) => {
        if (valid) {
          const { randomToken, normalForm } = this
          const { password } = normalForm
          this.normalLogin({ ...this.normalForm, randomToken, password: this.encodePassword(password) })
        }
      })
    },
    // 用户信息编辑
    async getUserInfo(data) {
      const res = await accountManageServer.getUserInfo()
      this.loading = false
      if (res.code === 0 && res.data) {
        this.SET_USERINFO({ ...data, ...res.data })
      }
    },
    // 账号注册
    async normalLogin(params) {
      const res = await loginManageServer.login(params)
      console.log(res)
      if (res.code === 0) {
        this.$message({ type: 'success', message: '登录成功!' })
        const { jwt } = res.data
        console.log(res)
        const { user: userData } = jwtDecode(jwt)
        console.log(JSON.parse(userData), 'JSON.parse(userData)')
        const { groupAdminName, username, roleName, groupInfos = [] } = JSON.parse(userData)
        this.SET_AUTHORIZATION(jwt)
        this.SET_USERID(username)
        this.getUserInfo(JSON.parse(userData))
        this.SET_GROUPLIST(groupInfos)
        console.log(groupAdminName, roleName)
        const isGroupAdmin = groupInfos.map((v) => v.groupAdminName).includes(username)
        // FIXME:
        if (roleName === 'admin_user') {
          this.SET_PERMISSION(permissionMap.admin_user)
        } else if (isGroupAdmin) {
          this.SET_PERMISSION(permissionMap.group_admin)
        } else {
          this.SET_PERMISSION(permissionMap[roleName])
        }
        // this.SET_PERMISSION(permissionMap.agency_admin)
        this.getConfig()
      } else {
        this.getImageCode()
      }
    },
    async getConfig() {
      const res = await settingManageServer.queryAgencyMetas({
        condition: {
          name: '',
          cnName: '',
          desc: '',
          meta: '',
          createTime: ''
        },
        pageNum: 1,
        pageSize: 999
      })
      if (res.code === 0 && res.data) {
        const { dataList } = res.data
        const agencyList = dataList.map((v) => {
          return {
            label: v.cnName,
            value: v.name
          }
        })
        this.SET_AGENCYLIST(agencyList)
        this.getConfigAlgList()
      }
    },
    async getConfigAlgList() {
      const res = await settingManageServer.getConfig({ key: 'wedpr_algorithm_templates' })
      if (res.code === 0 && res.data) {
        const realData = JSON.parse(res.data)
        const supportAlg = realData.templates.map((v) => v.name)
        console.log(supportAlg, 'supportAlg', realData.templates)
        this.SET_ALGLIST(algListFull.filter((v) => supportAlg.includes(v.value)))
        this.userAgency()
      }
    },

    // 查询机构信息
    async userAgency() {
      const res = await loginManageServer.userAgency()
      console.log(res)
      if (res.code === 0) {
        console.log(res)
        const { agencyName, agencyAdminName } = res.data
        this.SET_AGENCYNAME(agencyName)
        this.SET_AGENCYID(agencyName)
        this.SET_AGENCYADMIN(agencyAdminName)
        const { redirectUrl } = this.$route
        if (redirectUrl) {
          this.$router.push({ path: decodeURIComponent(redirectUrl) })
        } else {
          this.$router.push({ path: '/home' })
        }
      }
    },
    goRegister() {
      this.$router.push('/register')
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
  .code {
    width: auto;
    margin-left: 10px;
    height: 38px;
    vertical-align: middle;
  }
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
