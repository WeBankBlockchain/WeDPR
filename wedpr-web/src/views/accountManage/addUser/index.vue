<template>
  <el-dialog width="462px" title="新增用户" @close="handleClose" :visible="showAddModal">
    <div class="form-con">
      <el-form label-position="right" size="small" :model="userForm" :rules="userFormRules" ref="userForm" :label-width="formLabelWidth">
        <el-form-item label="用户名称：" prop="username">
          <el-select loading-text="搜索中" filterable style="width: 326px" v-model="userForm.username" remote :remote-method="getUserNameSelect" placeholder="请选择" clearable>
            <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
          </el-select>
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
import { userSelect } from 'Mixin/userSelect.js'
export default {
  name: 'addUser',
  mixins: [userSelect],
  props: {
    showAddModal: {
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
        username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }]
      },
      formLabelWidth: '96px',
      loading: false
    }
  },
  methods: {
    handleClose() {
      this.$emit('closeModal')
    },
    async getUserNameSelect(username) {
      if (!username) {
        this.userNameSelectList = []
        return
      }
      const res = await accountManageServer.getUser({ pageNum: 1, pageSize: 9999, username })
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
    // 创建账号
    async addUser(params) {
      const res = await accountManageServer.addUser(params)
      this.loading = false
      if (res.code === 0) {
        this.userNameSelectList = []
        this.$message({ type: 'success', message: '用户添加成功' })
        this.$emit('handlOK')
      }
    },
    handleOk() {
      this.loading = true
      const { groupId } = this
      this.$refs.userForm.validate((valid) => {
        if (valid) {
          this.addUser({ userList: [{ ...this.userForm, groupId }], groupId })
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
