<template>
  <div class="create-data">
    <el-form :inline="false" :rules="rules" :model="dataForm" ref="dataForm" size="small">
      <formCard title="基础信息">
        <el-form-item label-width="96px" label="资源名称：" prop="datasetTitle">
          <el-input style="width: 480px" placeholder="请输入资源名称" v-model="dataForm.datasetTitle" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="96px" label="资源简介：" prop="datasetDesc">
          <el-input style="width: 480px" placeholder="请输入资源简介" v-model="dataForm.datasetDesc" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="96px" label="资源标签：" prop="datasetLabel">
          <el-input style="width: 480px" placeholder="请输入资源标签" v-model="dataForm.datasetLabel" autocomplete="off"></el-input>
        </el-form-item>
      </formCard>
      <formCard title="资源权限">
        <el-form-item label-width="96px" label="可见范围：" prop="datasetVisibility">
          <el-radio-group v-model="dataForm.datasetVisibility">
            <el-radio :label="0">私有</el-radio>
            <el-radio :label="1">公开可用</el-radio>
            （选择可见范围）
          </el-radio-group>
        </el-form-item>
        <el-form-item label-width="96px" label="设定：" prop="setting" v-if="dataForm.datasetVisibility">
          <el-checkbox-group v-model="dataForm.setting">
            <el-checkbox label="本机构内" value="selfAgency"></el-checkbox>
            <div style="display: flex; align-items: center">
              <el-checkbox label="本用户组内" value="selfUserGroup"></el-checkbox>
              <el-select
                v-if="dataForm.setting.includes('本用户组内')"
                size="small"
                multiple
                style="width: 400px; margin-left: 16px; margin-top: -12px"
                v-model="dataForm.groupIdList"
                placeholder="请选择"
                clearable
              >
                <el-option v-for="item in groupList" :label="item.groupName" :value="item.groupId" :key="item.value"></el-option>
              </el-select>
            </div>
            <div style="display: flex; align-items: center">
              <el-checkbox label="指定机构" value="agencyList"> 指定机构 </el-checkbox>
              <el-select
                v-if="dataForm.setting.includes('指定机构')"
                size="small"
                multiple
                style="width: 400px; margin-left: 16px; margin-top: -12px"
                v-model="dataForm.agencyList"
                placeholder="请选择"
                clearable
              >
                <el-option v-for="item in agencyList" :label="item.label" :value="item.value" :key="item.value"></el-option>
              </el-select>
            </div>
            <el-checkbox label="指定用户" value="userList"> 指定用户 </el-checkbox>
            <div v-if="dataForm.setting.includes('指定用户')">
              <div v-for="(item, i) in dataForm.userList" :key="item.agency" style="display: flex; margin-bottom: 18px">
                <el-select size="small" style="width: 160px; margin-left: 16px" v-model="item.agency" placeholder="请选择机构">
                  <el-option v-for="item in agencyList" :label="item.label" :value="item.value" :key="item.value"></el-option>
                </el-select>
                <el-input
                  v-if="agencyId !== item.agency"
                  size="small"
                  style="width: 360px; margin-left: 16px"
                  placeholder="请输入用户名，多个用户名用“,”隔开"
                  v-model="item.user"
                  autocomplete="off"
                ></el-input>
                <el-select
                  v-if="agencyId === item.agency"
                  multiple
                  loading-text="搜索中"
                  filterable
                  style="width: 360px; margin-left: 16px"
                  v-model="item.user"
                  remote
                  :remote-method="getUserNameSelect"
                  placeholder="请选择"
                  clearable
                >
                  <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
                </el-select>
                <el-button style="margin-left: 16px; color: red" type="text" @click="deleteUser(i)">删除</el-button>
              </div>
              <el-button type="primary" style="margin: 16px; margin-top: 0" @click="addUser">增加用户</el-button>
            </div>

            <el-checkbox label="全局" value="global"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </formCard>
    </el-form>
    <div>
      <el-button size="medium" @click="back"> 取消 </el-button>
      <el-button size="medium" icon="el-icon-edit" type="primary" @click="submit"> 确认编辑 </el-button>
    </div>
  </div>
</template>
<script>
import { dataManageServer } from 'Api'
import formCard from '@/components/formCard.vue'
import { SET_FILEUPLOADTASK } from 'Store/mutation-types.js'
import { mapMutations, mapGetters } from 'vuex'
import { userSelect } from 'Mixin/userSelect.js'
export default {
  name: 'dataCreate',
  mixins: [userSelect],
  components: {
    formCard
  },
  data() {
    return {
      loadingFlag: false,
      showAddModal: false,
      dataForm: {
        datasetTitle: '',
        datasetDesc: '',
        datasetLabel: '',
        datasetVisibility: 0,
        setting: [],
        agencyList: '',
        filePath: '',
        userList: [{}],
        groupIdList: []
      },
      rangeMap: {
        本用户组内: 'selfUserGroup',
        本机构内: 'selfAgency',
        指定机构: 'agencyList',
        指定用户: 'userList',
        全局: 'global'
      },
      datasetId: ''
    }
  },
  created() {
    const { datasetId } = this.$route.query
    this.datasetId = datasetId
    this.getDetail()
  },
  computed: {
    ...mapGetters(['fileUploadTask', 'agencyList', 'agencyId', 'groupList']),

    rules() {
      return {
        datasetTitle: [
          {
            required: true,
            message: '请输入资源标题',
            trigger: 'blur'
          }
        ],
        datasetDesc: [
          {
            required: true,
            message: '请输入资源描述',
            trigger: 'blur'
          }
        ],
        datasetLabel: [
          {
            required: true,
            message: '请输入资源标签',
            trigger: 'blur'
          }
        ],
        datasetVisibility: [
          {
            required: true,
            message: '请输入访问文件URL',
            trigger: 'blur'
          }
        ],
        setting: [
          {
            required: this.dataForm.datasetVisibility === 1,
            validator: this.validateSetting,
            trigger: 'blur'
          }
        ]
      }
    }
  },
  methods: {
    ...mapMutations([SET_FILEUPLOADTASK]),
    // 获取数据集详情
    async getDetail() {
      this.loadingFlag = true
      const { datasetId } = this
      const res = await dataManageServer.queryDataset({ datasetId })
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { visibilityDetails, datasetTitle = '', datasetDesc = '', datasetLabel = '' } = res.data
        const visibilityDetailsData = JSON.parse(visibilityDetails)
        const { userList = [] } = visibilityDetailsData
        const dataForm = { datasetTitle, datasetDesc, datasetLabel, ...visibilityDetailsData, setting: [] }
        Object.keys(this.rangeMap).forEach((key) => {
          const Value = this.rangeMap[key]
          if (dataForm[Value]) {
            dataForm.setting.push(key)
          }
        })
        dataForm.userList = userList.map((v) => {
          if (v.agency === this.agencyId) {
            return { ...v, user: v.user.split(',') }
          } else {
            return { ...v }
          }
        })
        this.dataForm = { ...dataForm }
        console.log(this.dataForm, ' this.dataForm')
      }
    },
    addUser() {
      this.dataForm.userList.push({})
    },
    deleteUser(i) {
      this.dataForm.userList.splice(i, 1)
    },
    validateSetting(rule, value, callback) {
      if (value && value.length) {
        if (value.includes('指定机构') && !this.dataForm.agencyList.length) {
          return callback(new Error('请选择机构'))
        }
        if (value.includes('本用户组内') && !this.dataForm.groupIdList.length) {
          return callback(new Error('请选择用户组'))
        }
        if (value.includes('指定用户')) {
          const validData = this.dataForm.userList.filter((v) => {
            return v.user.length && v.agency
          })
          if (validData.length) {
            callback()
          } else {
            return callback(new Error('请添加用户'))
          }
        }
        callback()
      } else {
        return callback(new Error('请选择范围'))
      }
    },
    async updateDataset(params) {
      const res = await dataManageServer.updateDataset(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('数据集编辑成功')
        this.getDetail()
      }
    },
    handleTypeChange(data) {
      console.log(data)
    },
    back() {
      history.back()
    },
    submit() {
      console.log(this.$refs.dataForm.validate())
      try {
        this.$refs.dataForm.validate((valid) => {
          console.log(this.dataForm)
          if (valid) {
            console.log(this.dataForm)
            const { datasetTitle, datasetDesc, datasetLabel, datasetVisibility, setting, agencyList, userList, groupIdList } = this.dataForm
            const params = { datasetTitle, datasetDesc, datasetLabel, datasetVisibility }
            const datasetVisibilityDetails = { datasetVisibility }
            let permissionDes = []
            if (datasetVisibility) {
              const settingValue = setting.map((v) => this.rangeMap[v])
              settingValue.forEach((v) => {
                datasetVisibilityDetails[v] = true
              })
              if (settingValue.includes('global')) {
                permissionDes = [...permissionDes, '全局']
              }
              if (settingValue.includes('selfAgency')) {
                permissionDes = [...permissionDes, '本机构内']
              }
              if (settingValue.includes('agencyList')) {
                datasetVisibilityDetails.agencyList = agencyList
                permissionDes = [...permissionDes, ...agencyList.map((v) => v + '机构')]
              }
              if (settingValue.includes('selfUserGroup')) {
                datasetVisibilityDetails.groupIdList = groupIdList
                const dataTags = this.groupList.filter((v) => groupIdList.includes(v.groupId)).map((v) => v.groupName + '(' + this.agencyId + '机构)')
                permissionDes = [...permissionDes, ...dataTags]
              }
              if (settingValue.includes('userList')) {
                datasetVisibilityDetails.userList = userList.map((v) => {
                  if (v.agency === this.agencyId) {
                    return { ...v, user: v.user.join(',') }
                  } else {
                    return { ...v }
                  }
                })
                const usersDesList = []
                datasetVisibilityDetails.userList.forEach((v) => {
                  const users = v.user.split(',')
                  users.forEach((name) => {
                    usersDesList.push(name + '(' + v.agency + '机构)')
                  })
                })
                permissionDes = [...permissionDes, ...usersDesList]
              }
            }
            // 权限中文描述
            datasetVisibilityDetails.permissionDes = permissionDes
            this.updateDataset({ ...params, datasetVisibilityDetails, datasetId: this.datasetId })
          }
        })
      } catch (error) {
        console.log(error)
      }
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
::v-deep .el-input-group__prepend {
  width: 94px;
  text-align: left;
}
</style>
