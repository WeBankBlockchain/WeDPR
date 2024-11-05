<template>
  <el-dialog width="462px" title="修改密码" @close="handleClose" :visible="showModifyModal">
    <div class="form-con">
      <el-form label-position="right" size="small" :model="userForm" :rules="userFormRules" ref="userForm" :label-width="formLabelWidth">
        <el-form-item prop="oldPassword" label="旧的密码">
          <el-input v-model="userForm.oldPassword" placeholder="请输入" show-password style="width: 280px">
            <template slot="prepend"> 密码 </template>
          </el-input>
        </el-form-item>

        <el-form-item prop="newPassword" label="新的密码：">
          <el-tooltip class="item" effect="dark" :content="passwordtips" placement="bottom">
            <el-input v-model="userForm.newPassword" placeholder="请输入" show-password style="width: 280px">
              <template slot="prepend"> 密码 </template>
            </el-input>
          </el-tooltip>
        </el-form-item>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button :loading="loading" type="primary" @click="handleOk">确 定</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { accountManageServer } from 'Api'
import { mapGetters } from 'vuex'
const sm2 = require('sm-crypto').sm2
export default {
  name: 'addUser',
  props: {
    showModifyModal: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      userForm: {
        oldPassword: '',
        newPassword: ''
      },
      passwordtips: '密码长度8~18个字符，支持数字、大小写字母、特殊字符\'-!"#$%&()*,./:;?@[]^_`{|}~+<=>，至少包含一个数字和一个大写字母和一个小写字母和一个特殊字符',
      userFormRules: {
        oldPassword: [{ required: true, message: '旧密码不能为空', trigger: 'blur' }],
        newPassword: [{ required: true, validator: this.validPassword, trigger: 'blur' }]
      },
      formLabelWidth: '96px',
      loading: false
    }
  },
  computed: {
    ...mapGetters(['userinfo', 'pbKey'])
  },
  methods: {
    encodePassword(password) {
      const { pbKey } = this
      const cipherMode = 1
      const encryptedPassword = sm2.doEncrypt(password, pbKey, cipherMode)
      return encryptedPassword
    },
    validPassword(rule, str, callback) {
      if (!str) {
        return callback(new Error('密码不能为空'))
      } else if (!/^(?=.*[0-9])(?=.*[A-Z])(?=.*[a-z])(?=.*[\W_])(?=.*[\S])^[0-9A-Za-z\S]{8,18}$/.test(str)) {
        return callback(new Error('密码格式有误'))
      } else {
        callback()
      }
    },
    handleClose() {
      this.$emit('closeModal')
    },
    // 用户信息编辑
    async modifyPassword(params) {
      const res = await accountManageServer.modifyPassword(params)
      this.loading = false
      if (res.code === 0) {
        this.$message({ type: 'success', message: '用户密码编辑成功' })
        this.$emit('handlOK')
      }
    },
    handleOk() {
      this.loading = true
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          const { oldPassword = '', newPassword = '' } = this.userForm
          this.modifyPassword({ oldPassword: this.encodePassword(oldPassword), newPassword: this.encodePassword(newPassword) })
        } else {
          this.loading = false
        }
      })
    }
  }
}
</script>
<style lang="less" scoped>
div.form-con {
  .el-upload-list__item-name {
    text-align: left;
  }
  div.type-con {
    margin-bottom: 18px;
    .type-label {
      width: 110px;
      display: inline-block;
      text-align: right;
      margin-right: 12px;
    }
  }
}
</style>
