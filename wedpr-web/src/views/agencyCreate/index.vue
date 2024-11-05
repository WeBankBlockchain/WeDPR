<template>
  <div class="create-data">
    <el-form :inline="false" @submit="queryHandle" :rules="rules" :model="dataForm" ref="dataForm" size="small">
      <el-form-item label-width="128px" label="机构名称：" prop="agencyName">
        <el-input :disabled="!!agencyId" style="width: 480px" placeholder="请输入" v-model="dataForm.agencyName" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label-width="128px" label="机构简介：" prop="agencyDesc">
        <el-input type="textarea" :autosize="{ minRows: 4 }" style="width: 480px" placeholder="请输入" v-model="dataForm.agencyDesc" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label-width="128px" label="机构联系人：" prop="agencyContact">
        <el-input type="phone" style="width: 480px" placeholder="请输入" v-model="dataForm.agencyContact" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label-width="128px" label="机构联系方式：" prop="contactPhone">
        <el-input type="phone" style="width: 480px" placeholder="请输入" v-model="dataForm.contactPhone" autocomplete="off"></el-input>
      </el-form-item>
      <el-form-item label-width="128px" label="机构网关地址：" prop="gatewayEndpoint">
        <el-input style="width: 480px" placeholder="请输入" v-model="dataForm.gatewayEndpoint" autocomplete="off"></el-input>
      </el-form-item>
    </el-form>
    <div style="padding-left: 128px">
      <el-button size="medium" icon="el-icon-plus" type="primary" @click="submit" v-if="!agencyId"> 确认新增 </el-button>
      <el-button size="medium" icon="el-icon-edit" type="primary" @click="submit" v-if="agencyId"> 确认编辑 </el-button>
    </div>
  </div>
</template>
<script>
import { agencyManageServer } from 'Api'
export default {
  name: 'projectCreate',
  data() {
    return {
      dataForm: {
        agencyName: '',
        agencyDesc: '',
        agencyContact: '',
        contactPhone: '',
        gatewayEndpoint: ''
      },
      rules: {
        agencyName: [{ required: true, message: '机构名称不能为空', trigger: 'blur' }],
        agencyDesc: [{ required: true, message: '机构简介不能为空', trigger: 'blur' }],
        agencyContact: [{ required: true, message: '机构联系人不能为空', trigger: 'blur' }],
        contactPhone: [{ required: true, message: '机构联系方式不能为空', trigger: 'blur' }],
        gatewayEndpoint: [{ required: true, validator: this.validateGateway, trigger: 'blur' }]
      },
      agencyId: ''
    }
  },
  created() {
    const { agencyId = '' } = this.$route.query
    if (agencyId) {
      this.agencyId = agencyId
      this.getAgencyDetail()
    }
  },
  methods: {
    // 获取项目详情
    async getAgencyDetail() {
      this.loadingFlag = true
      const { agencyId } = this
      const res = await agencyManageServer.getAgencyDetail({ agencyId })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        this.dataForm = { ...res.data }
      }
    },
    async createAgency(params) {
      const res = await agencyManageServer.createAgency(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success(this.agencyId ? '机构编辑成功' : '机构新增成功')
        if (this.agencyId) {
          history.go(-1)
        } else {
          this.$router.push({ path: '/agencyManage' })
        }
      }
    },
    validateGateway(rule, value, callback) {
      if (!value) {
        return callback(new Error('机构网关地址不能为空'))
      } else if (!/^([a-zA-Z0-9.-]+|\d{1,3}(\.\d{1,3}){3}):\d{1,5}$/.test(value)) {
        return callback(new Error('机构网关地址格式有误'))
      } else {
        callback()
      }
    },
    submit() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const params = { ...this.dataForm }
          if (this.agencyId) {
            params.agencyId = this.agencyId
          }
          this.createAgency(params)
        }
      })
    }
  }
}
</script>
<style lang="less" scoped>
div.create-data {
  .el-checkbox {
    display: block;
    margin-bottom: 16px;
  }
}
</style>
