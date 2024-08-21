<template>
  <el-dialog width="450px" title="新增用户组" @close="handleClose" :visible="showAddModal">
    <div class="form-con">
      <el-form label-position="right" size="small" :model="tenantForm" :rules="tenantFormRules" ref="tenantForm" :label-width="formLabelWidth">
        <el-form-item label="用户组名称：" prop="groupName">
          <el-input style="width: 298px" placeholder="请输入租户名称" v-model="tenantForm.groupName" autocomplete="off"></el-input>
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
import { passwordHanle } from 'Mixin/passwordHandle.js'
export default {
  name: 'addGroup',
  mixins: [passwordHanle],
  props: {
    showAddModal: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      tenantForm: {
        groupName: ''
      },
      tenantFormRules: {
        groupName: [{ required: true, message: '用户组名不能为空', trigger: 'blur' }]
      },
      formLabelWidth: '110px',
      loading: false
    }
  },
  methods: {
    handleClose() {
      this.$emit('closeModal')
    },
    // 创建用户组
    async createTenant(params) {
      const res = await accountManageServer.addGroup(params)
      this.loading = false
      if (res.code === 0) {
        this.$message({ type: 'success', message: '用户组创建成功' })
        this.$emit('handlOK')
      }
    },
    handleOk() {
      this.$refs.tenantForm.validate((valid) => {
        if (valid) {
          this.createTenant({ ...this.tenantForm })
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
