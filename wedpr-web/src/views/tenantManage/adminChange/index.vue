<template>
  <el-dialog width="450px" title="管理员变更" @close="handleClose" :visible="showChangeModal">
    <div class="form-con">
      <el-form label-position="right" size="small" :model="userForm" :rules="userFormRules" ref="userForm" :label-width="formLabelWidth">
        <el-form-item label="选择用户：" prop="username">
          <el-select style="width: 298px" v-model="userForm.username" placeholder="请选择" clearable>
            <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </div>
    <div slot="footer" class="dialog-footer">
      <el-button size="small" @click="handleClose">取 消</el-button>
      <el-button size="small" :loading="loading" type="primary" @click="handleOk">确 定</el-button>
    </div>
  </el-dialog>
</template>
<script>
import { accountManageServer } from 'Api'
export default {
  name: 'adminChange',
  props: {
    showChangeModal: {
      type: Boolean,
      default: false
    },
    groupId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      userForm: {
        username: ''
      },
      userFormRules: {
        username: [{ required: true, message: '管理员不能为空', trigger: 'blur' }]
      },
      formLabelWidth: '100px',
      userNameSelectList: [],
      pageData: { page_offset: 1, page_size: 10 },
      loading: false
    }
  },
  created() {
    this.groupId && this.getAccountList()
  },
  watch: {
    groupId(v) {
      v && this.getAccountList()
    }
  },
  methods: {
    handleClose() {
      this.$emit('closeModal')
    },
    // 创建普通投票管理员账号
    async setAdminUser(params) {
      this.loading = true
      const res = await accountManageServer.setAdminUser(params)
      this.loading = false
      if (res.code === 0) {
        this.$message({ type: 'success', message: '投票管理员更新成功' })
        this.$emit('handlOK')
      }
    },
    // 获取用户组成员下拉列表
    async getAccountList() {
      const { page_offset } = this.pageData
      const { groupId } = this
      const params = { pageNum: page_offset, pageSize: 20, groupId }
      const res = await accountManageServer.getGroupDetail(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        const { userList = [] } = res.data
        this.userNameSelectList = userList.map((v) => {
          return {
            label: v.username,
            value: v.username
          }
        })
      } else {
        this.userNameSelectList = []
      }
    },
    handleOk() {
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          this.setAdminUser({ ...this.userForm, groupId: this.groupId })
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
