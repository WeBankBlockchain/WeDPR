<template>
  <el-dialog width="462px" title="编辑用户信息" @close="handleClose" :visible="showModifyModal">
    <div class="form-con">
      <el-form label-position="right" size="small" :model="userForm" :rules="userFormRules" ref="userForm" :label-width="formLabelWidth">
        <el-form-item prop="phone" label="电话号码：">
          <el-input type="text" v-model="userForm.phone" placeholder="请输入电话号码"> </el-input>
        </el-form-item>
        <el-form-item prop="email" label="邮箱地址：">
          <el-input type="text" v-model="userForm.email" placeholder="请输入邮箱地址"> </el-input>
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
export default {
  name: 'addUser',
  props: {
    showModifyModal: {
      type: Boolean,
      default: false
    },
    userinfo: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      userForm: {
        phone: '',
        email: ''
      },
      userFormRules: {
        phone: [{ required: true, validator: this.validateMobile, trigger: 'blur' }],
        email: [{ required: true, message: '邮箱不能为空', trigger: 'blur' }]
      },
      formLabelWidth: '96px',
      loading: false
    }
  },
  created() {
    const { phone = '', email = '' } = this.userinfo
    this.userForm = { phone, email }
  },
  watch: {
    userinfo(v) {
      const { phone = '', email = '' } = v
      this.userForm = { phone, email }
    }
  },
  methods: {
    validateMobile(rule, value, callback) {
      if (!value) {
        return callback(new Error('手机号不能为空'))
      } else if (!/^\d{1,}$/.test(value)) {
        return callback(new Error('手机号格式有误'))
      } else {
        callback()
      }
    },
    handleClose() {
      this.$emit('closeModal')
    },
    // 用户信息编辑
    async modifyUser(params) {
      const res = await accountManageServer.modifyUser(params)
      this.loading = false
      if (res.code === 0) {
        this.$message({ type: 'success', message: '用户信息编辑成功' })
        this.$emit('handlOK')
      }
    },
    handleOk() {
      this.loading = true
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          this.modifyUser({ ...this.userForm })
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
