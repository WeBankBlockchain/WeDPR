<template>
  <div class="group-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item v-if="hasGroupAdminPermission">
          <el-button type="primary" icon="el-icon-plus" @click="showAddUser"> 新增用户 </el-button>
        </el-form-item>
        <div style="float: right">
          <el-form-item prop="username" label="用户名：">
            <el-select loading-text="搜索中" filterable style="width: 160px" v-model="searchForm.username" remote :remote-method="getUserNameSelect" placeholder="请选择" clearable>
              <el-option v-for="item in userNameSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="queryFlag" @click="queryHandle">
              {{ queryFlag ? '查询中...' : '查询' }}
            </el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="default" :loading="queryFlag" @click="reset"> 重置 </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="tableContent autoTableWrap" v-if="total">
      <el-table :max-height="tableHeight" size="small" v-loading="loadingFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column label="用户名" prop="username" show-overflow-tooltip>
          <template v-slot="scope">
            {{ scope.row.username }}
            <el-tag style="margin-left: 10px" size="small" color="#3071F2;" v-if="scope.row.username === groupAdminName">组管理员</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="权限" prop="roleName" show-overflow-tooltip>
          <template v-slot="scope">
            {{ scope.row.roleName === 'admin_user' ? '管理员' : '普通用户' }}
          </template>
        </el-table-column>
        <el-table-column label="联系方式" prop="phone" show-overflow-tooltip />
        <el-table-column label="邮箱" prop="email" show-overflow-tooltip />
        <el-table-column label="新增时间" prop="createTime" show-overflow-tooltip />
        <el-table-column label="操作" width="180px">
          <template v-slot="scope">
            <el-button v-if="hasGroupAdminPermission && groupAdminName !== scope.row.username" @click="showDelModal(scope.row)" size="small" type="text">删除用户</el-button>
          </template>
        </el-table-column>
      </el-table>
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
    <addUser :groupId="groupId" :showAddModal="showAddModal" @closeModal="closeModal" @handlOK="handlOK" />
    <adminChange :groupId="groupId" :showChangeModal="showChangeModal" @closeModal="closeChangeModal" @handlOK="handlOK" />
    <el-empty v-if="!total" :image-size="120" description="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
  </div>
</template>
<script>
import { accountManageServer } from 'Api'
import { SET_USERINFO } from 'Store/mutation-types.js'
import { mapMutations, mapGetters } from 'vuex'
import { handleParamsValid } from 'Utils/index.js'
import addUser from './addUser'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import adminChange from '../tenantManage/adminChange'
export default {
  name: 'groupManage',
  mixins: [tableHeightHandle],
  components: {
    addUser,
    adminChange
  },
  data() {
    return {
      searchForm: {
        username: '',
        role_name: ''
      },
      searchQuery: {
        username: '',
        role_name: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 20
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      showChangeModal: false,
      groupId: '',
      userNameSelectList: [],
      groupAdminName: ''
    }
  },
  created() {
    const { groupId } = this.$route.query
    this.groupId = groupId || this.userinfo.groupId
    this.groupId && this.getAccountList()
    this.groupId && this.getGroupList()
  },
  computed: {
    ...mapGetters(['userinfo', 'userId']),
    hasGroupAdminPermission() {
      return this.groupAdminName === this.userId || this.userinfo.roleName === 'admin_user'
    }
  },
  methods: {
    ...mapMutations([SET_USERINFO]),
    reset() {
      this.$refs.searchForm.resetFields()
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getAccountList()
        } else {
          return false
        }
      })
    },
    // 获取群组信息
    async getGroupList() {
      const { groupId = '' } = this
      const params = { groupId, pageNum: 1, pageSize: 10 }
      this.queryFlag = true
      const res = await accountManageServer.getGroupList(params)
      this.queryFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { groupList = [] } = res.data
        if (groupList.length) {
          const { adminName } = groupList[0]
          this.groupAdminName = adminName
          // this.SET_USERINFO({ ...this.userInfo, groupAdminName: adminName })
        }
      }
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getAccountList()
    },
    async getUserNameSelect(username) {
      if (!username) {
        this.userNameSelectList = []
        return
      }
      const { groupId } = this
      const res = await accountManageServer.getGroupDetail({ pageNum: 1, pageSize: 9999, username, groupId })
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
    // 获取账户列表
    async getAccountList() {
      const { page_offset, page_size } = this.pageData
      const { username, role_name } = this.searchQuery
      const { groupId } = this
      let params = handleParamsValid({ username, role_name })
      params = { ...params, pageNum: page_offset, pageSize: page_size, groupId }
      this.loadingFlag = true
      const res = await accountManageServer.getGroupDetail(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { userList = [], total } = res.data
        this.tableData = userList
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 删除账户
    showDelModal(tenant) {
      const { username } = tenant
      const { groupId = '' } = this
      this.$confirm(`确认删除用户--'${username}'?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteAccount({ username, groupId })
        })
        .catch(() => {})
    },
    async deleteAccount(params) {
      const res = await accountManageServer.delUser(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('用户删除成功')
        this.getAccountList()
      }
    },

    showAddUser() {
      console.log(99999)
      this.showAddModal = true
    },
    closeModal() {
      this.showAddModal = false
    },
    closeChangeModal() {
      this.showChangeModal = false
    },
    showAdminChange() {
      this.showChangeModal = true
    },
    handlOK() {
      this.showAddModal = false
      this.showChangeModal = false
      this.getAccountList()
      this.getGroupList()
    }
  }
}
</script>
<style lang="less" scoped></style>
