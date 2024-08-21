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
      <formCard title="资源来源">
        <el-form-item label-width="126px" label="数据对接来源：" prop="dataSourceType">
          <el-cascader @change="handleTypeChange" style="width: 480px" v-model="dataForm.dataSourceType" :options="typeList" :props="{ expandTrigger: 'hover' }"></el-cascader>
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('CSV')" label-width="126px" label="上传文件：" prop="dataFile">
          <weUpLoad key="dataCsvFile" accept=".csv" tips="将csv文件拖到此处，或点击此处上传" :beforeUpload="beforeUploadCsv" v-model="dataForm.dataFile"></weUpLoad>
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('EXCEL')" label-width="126px" label="上传文件：" prop="dataFile">
          <weUpLoad key="dataExcelFile" accept=".xls,.xlsx" tips="将excel文件拖到此处，或点击此处上传" :beforeUpload="beforeUploadExcel" v-model="dataForm.dataFile"></weUpLoad>
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('HIVE') || dataForm.dataSourceType.includes('DB')" label-width="126px" label="数据类型：" prop="dynamicType">
          <el-radio-group v-model="dataForm.dynamicType">
            <el-radio :label="0">静态数据</el-radio>
            <el-radio :label="1">动态数据</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('DB')" label-width="126px" label="数据库信息：" prop="databaseInfo">
          <el-form-item prop="ip">
            <el-input v-model="dataForm.ip" placeholder="请输入" style="width: 480px">
              <template slot="prepend"> IP地址 </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="port">
            <el-input v-model="dataForm.port" placeholder="请输入" style="width: 480px">
              <template slot="prepend"> 端口号 </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="databaseName">
            <el-input v-model="dataForm.databaseName" placeholder="请输入" style="width: 480px">
              <template slot="prepend"> 数据库名 </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="userName">
            <el-input v-model="dataForm.userName" placeholder="请输入" style="width: 480px">
              <template slot="prepend"> 用户名 </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="dataForm.password" placeholder="请输入" show-password style="width: 480px">
              <template slot="prepend"> 密码 </template>
            </el-input>
          </el-form-item>
          <el-form-item prop="sql">
            <el-input
              v-model="dataForm.sql"
              placeholder="请输入SQL语句查询数据集，支持标准MySQL语句，示例：select id, x1, x2 from test limit 0, 10"
              type="textarea"
              :autosize="{ minRows: 4 }"
              style="width: 480px"
              @focus="onSqlFocus"
            />
          </el-form-item>
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('HIVE')" label-width="126px" label="数据库信息：" prop="sql">
          <el-input v-model="dataForm.sql" placeholder="请输入" type="textarea" :autosize="{ minRows: 4 }" style="width: 480px" @focus="onSqlFocus" />
        </el-form-item>
        <el-form-item v-if="dataForm.dataSourceType.includes('HDFS')" label-width="126px" label="访问文件URL：" prop="filePath">
          <el-input v-model="dataForm.filePath" placeholder="请输入访问文件URL" type="text" style="width: 480px" />
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
      <el-button size="medium" icon="el-icon-plus" type="primary" @click="submit"> 确认创建 </el-button>
    </div>
  </div>
</template>
<script>
import { dataManageServer } from 'Api'
import formCard from '@/components/formCard.vue'
import weUpLoad from '@/components/upLoad.vue'
import { SET_FILEUPLOADTASK } from 'Store/mutation-types.js'
import { mapMutations, mapGetters } from 'vuex'
import { userSelect } from 'Mixin/userSelect.js'
export default {
  name: 'dataCreate',
  mixins: [userSelect],
  components: {
    formCard,
    weUpLoad
  },
  data() {
    return {
      loadingFlag: false,
      showAddModal: false,
      typeList: [],
      dataForm: {
        datasetTitle: '',
        datasetDesc: '',
        datasetLabel: '',
        dataSourceType: [],
        ip: '',
        port: '',
        databaseName: '',
        userName: '',
        password: '',
        sql: '',
        datasetVisibility: 0,
        setting: [],
        agencyList: '',
        dataFile: null,
        dynamicType: '',
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
      rangeMapLabel: {
        selfUserGroup: '本用户组内',
        selfAgency: '本机构内',
        agencyList: '指定机构',
        userList: '指定用户',
        global: '全局'
      },
      datasetId: ''
    }
  },
  created() {
    this.getDataUploadType()
    const { datasetId } = this.$route.query
    this.datasetId = datasetId
    datasetId && this.getDetail({ datasetId })
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
        dataSourceType: [
          {
            required: true,
            message: '请选择数据对接来源',
            trigger: 'blur'
          }
        ],
        ip: [
          {
            required: this.dataForm.dataSourceType.includes('DB'),
            message: '请输入数据库IP地址',
            trigger: 'blur'
          }
        ],
        port: [
          {
            required: this.dataForm.dataSourceType.includes('DB'),
            message: '请输入数据库端口',
            trigger: 'blur'
          }
        ],
        databaseName: [
          {
            required: this.dataForm.dataSourceType.includes('DB'),
            message: '请输入数据库名称',
            trigger: 'blur'
          }
        ],
        userName: [
          {
            required: this.dataForm.dataSourceType.includes('DB'),
            message: '请输入数据库用户名',
            trigger: 'blur'
          }
        ],
        password: [
          {
            required: this.dataForm.dataSourceType.includes('DB'),
            message: '请输入数据库密码',
            trigger: 'blur'
          }
        ],
        sql: [
          {
            required: this.dataForm.dataSourceType.includes('DB') || this.dataForm.dataSourceType.includes('HIVE'),
            message: '请输入SQL语句',
            trigger: 'blur'
          }
        ],
        dataFile: [
          {
            required: this.dataForm.dataSourceType.includes('CSV') || this.dataForm.dataSourceType.includes('EXCEL'),
            message: '请上传文件',
            trigger: 'blur'
          }
        ],
        dynamicType: [
          {
            required: this.dataForm.dataSourceType.includes('HIVE') || this.dataForm.dataSourceType.includes('DB'),
            message: '请选择数据类型',
            trigger: 'blur'
          }
        ],
        filePath: [
          {
            required: this.dataForm.dataSourceType.includes('HDFS'),
            message: '请输入访问文件URL',
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
    async getDetail(params) {
      this.loadingFlag = true
      const res = await dataManageServer.queryDataset(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { visibilityDetails, datasetTitle = '', datasetDesc = '', datasetLabel = '' } = res.data
        const visibilityDetailsData = JSON.parse(visibilityDetails)
        const { userList = [] } = visibilityDetailsData
        const dataForm = { ...this.dataForm, datasetTitle, datasetDesc, datasetLabel, ...visibilityDetailsData, setting: [] }
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
    beforeUploadCsv(file) {
      console.log(file, 1)
      const supportType = ['csv', 'text/csv']
      const isCSV = supportType.includes(file.type)
      const isLt2G = file.size / 1024 / 1024 < 2048
      if (!isCSV) {
        this.$message.error('文件只能是CSV格式!')
        return false
      }
      if (!isLt2G) {
        this.$message.error('文件大小不能超过 2G!')
        return false
      }
      return true
    },
    beforeUploadExcel(file) {
      console.log(file, 1)
      const supportType = ['xls', 'xlsx']
      const isCSV = supportType.includes(file.type)
      const isLt2G = file.size / 1024 / 1024 < 2048
      if (!isCSV) {
        this.$message.error('文件只能是xls, xlsx格式!')
        return false
      }
      if (!isLt2G) {
        this.$message.error('文件大小不能超过 2G!')
        return false
      }
      return true
    },
    onSqlFocus() {
      if (!this.dataForm.sql) {
        const { dataSourceType } = this.dataForm
        switch (dataSourceType) {
          case 'MYSQL':
            this.dataForm.sql = 'select id, x1, x2 from test'
            break
          case 'ORACLE':
            this.dataForm.sql = 'select id, x1, x2 from test'
            break
          case 'DM':
            this.dataForm.sql = 'select id, x1, x2 from test'
            break
          case 'GAUSS':
            this.dataForm.sql = 'select id, x1, x2 from test'
            break
          default:
            this.dataForm.sql = 'select id, x1, x2 from test'
            break
        }
      }
    },
    async getDataUploadType() {
      const res = await dataManageServer.getDataUploadType()
      console.log(res)
      if (res.code === 0 && res.data) {
        this.typeList = res.data
      }
    },
    async createDataset(params, fileParams) {
      const res = await dataManageServer.createDataset(params)
      console.log(res)
      if (res.code === 0 && res.data) {
        console.log('创建成功')
        const { datasetId } = res.data
        const { dataSourceType, dataFile } = fileParams
        if (dataSourceType === 'CSV' || dataSourceType === 'EXCEL') {
          const params = { dataSourceType, dataFile, datasetId, status: 'waitting', percentage: 0 }
          this.SET_FILEUPLOADTASK(params) // 上传任务推到队列
        }
        this.$message.success('数据集创建成功，开始上传数据')
        this.$router.push({ path: 'dataManage' })
      }
    },
    handleTypeChange(data) {
      console.log(data)
    },
    uploadCsvHandler({ file }) {
      console.log(file)
      const supportType = ['csv', 'text/csv']
      const isCSV = supportType.includes(file.type)
      const isLt2M = file.size / 1024 / 1024 < 1

      if (!isCSV) {
        this.$message.error('文件只能是CSV格式!')
        return
      }
      if (!isLt2M) {
        this.$message.error('文件大小不能超过 1MB!')
        return
      }
      this.dataForm.dataFile = file
    },
    beforeUploadPic(file) {
      console.log(file)
      const supportType = ['csv', 'text/csv']
      const isCSV = supportType.includes(file.type)
      const isLt2M = file.size / 1024 / 1024 < 1

      if (!isCSV) {
        this.$message.error('文件只能是CSV格式!')
        return false
      }
      if (!isLt2M) {
        this.$message.error('文件大小不能超过 1MB!')
        return false
      }
      return true
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
            const { datasetTitle, datasetDesc, datasetLabel, filePath, dataSourceType, datasetVisibility, setting, agencyList, userList, dataFile, groupIdList } = this.dataForm
            const sourceType = dataSourceType[0]
            const dbType = dataSourceType[1]
            const params = { datasetTitle, datasetDesc, datasetLabel, datasetVisibility, dataSourceType: sourceType }
            if (sourceType === 'DB') {
              const { ip, port, databaseName, userName, password, dynamicType } = this.dataForm
              params.dataSourceParams = { ...params, ip, port, databaseName, userName, password, dbType }
              params.dynamicType = dynamicType
            }
            if (sourceType === 'HDFS') {
              params.filePath = filePath
            }
            let permissionDes = []
            const datasetVisibilityDetails = { datasetVisibility }
            if (datasetVisibility) {
              const settingValue = setting.map((v) => this.rangeMap[v])
              settingValue.forEach((v) => {
                datasetVisibilityDetails[v] = true
                // permissionDes[v] = { title: this.rangeMapLabel[v] }
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
            this.datasetId && (params.datasetId = this.datasetId)
            this.createDataset({ ...params, datasetVisibilityDetails }, { dataSourceType: sourceType, dataFile })
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
// ::v-deep .el-cascader-menu__wrap {
//   height: 262px !important;
// }
</style>
