<template>
  <div class="group-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="showAddTenant" v-if="userinfo.roleName === 'admin_user'"> 新增用户组 </el-button>
        </el-form-item>
        <div style="float: right">
          <el-form-item prop="username" label="用户组名称：">
            <el-select
              loading-text="搜索中"
              filterable
              style="width: 160px"
              v-model="searchForm.groupName"
              remote
              :remote-method="getGroupNameSelect"
              placeholder="请选择"
              clearable
            >
              <el-option v-for="item in groupNameSelectList" :label="item.label" :value="item.label" :key="item.label"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item prop="username" label="用户组编号：">
            <el-select loading-text="搜索中" filterable remote :remote-method="getGroupIdSelect" style="width: 160px" v-model="searchForm.groupId" placeholder="请选择" clearable>
              <el-option v-for="item in groupIdSelectList" :label="item.label" :value="item.value" :key="item.value"></el-option>
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="queryFlag" @click="queryHandle">
              {{ queryFlag ? '查询中...' : '查询' }}
            </el-button>
          </el-form-item>
        </div>
      </el-form>
    </div>
    <div class="tableContent autoTableWrap">
      <el-table :max-height="tableHeight" size="small" v-loading="queryFlag" :data="tableData" :border="true" class="table-wrap">
        <el-table-column label="用户组编号" prop="groupId" show-overflow-tooltip />
        <el-table-column label="用户组名称" prop="groupName" show-overflow-tooltip />
        <el-table-column label="组管理员" prop="adminName" show-overflow-tooltip />
        <el-table-column label="组内用户数" prop="userCount" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" show-overflow-tooltip />
        <el-table-column label="操作" width="224px">
          <template v-slot="scope">
            <el-button @click="goDetail(scope.row)" size="small" type="text">查看用户组</el-button>
            <el-button v-if="userinfo.roleName === 'admin_user' || scope.row.adminName === userId" @click="showAdminChange(scope.row)" size="small" type="text"
              >管理员变更</el-button
            >
            <el-button v-if="userinfo.roleName === 'admin_user'" @click="showDelModal(scope.row)" size="small" type="text">删除组</el-button>
          </template>
        </el-table-column>
      </el-table>
      <addTenant :showAddModal="showAddModal" @closeModal="closeModal" @handlOK="handlOK" />
      <adminChange :groupId="modifyGroupId" :showChangeModal="showChangeModal" @closeModal="closeChangeModal" @handlOK="handlOK" />
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
  </div>
</template>
<script>
import { accountManageServer } from 'Api'
import wePagination from '@/components/wePagination.vue'
import addTenant from './addTenant'
import adminChange from './adminChange'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { handleParamsValid } from 'Utils/index.js'
import { mapGetters } from 'vuex'
export default {
  name: 'groupManage',
  mixins: [tableHeightHandle],
  components: {
    wePagination,
    addTenant,
    adminChange
  },
  data() {
    return {
      searchForm: {
        groupName: '',
        groupId: ''
      },
      searchQuery: {
        groupName: '',
        groupId: ''
      },
      pageData: {
        page_offset: 1,
        page_size: 10
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      showAddModal: false,
      showChangeModal: false,
      groupNameSelectList: [],
      groupIdSelectList: [],
      modifyGroupId: ''
    }
  },
  created() {
    this.getGroupList()
  },
  computed: {
    ...mapGetters(['userinfo', 'userId'])
  },
  methods: {
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.getGroupList()
        } else {
          return false
        }
      })
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getGroupList()
    },
    async getGroupNameSelect(groupName) {
      if (!groupName) {
        this.groupNameSelectList = []
        return
      }
      const res = await accountManageServer.getGroupList({ pageSize: 9999, pageNum: 1, groupName })
      if (res.code === 0 && res.data) {
        const { groupList = [] } = res.data
        this.groupNameSelectList = groupList.map((v) => {
          return {
            label: v.groupName,
            value: v.groupId
          }
        })
      }
    },
    async getGroupIdSelect(groupId) {
      if (!groupId) {
        this.groupIdSelectList = []
        return
      }
      const res = await accountManageServer.getGroupList({ pageSize: 9999, pageNum: 1, groupId })
      if (res.code === 0 && res.data) {
        const { groupList = [] } = res.data
        this.groupIdSelectList = groupList.map((v) => {
          return {
            label: v.groupId,
            value: v.groupId
          }
        })
      }
    },
    // 获取账户列表
    async getGroupList() {
      const { page_offset, page_size } = this.pageData
      const { groupName = '', groupId = '' } = this.searchQuery
      let params = handleParamsValid({ groupName, groupId })
      params = { ...params, pageNum: page_offset, pageSize: page_size }
      this.queryFlag = true
      const res = await accountManageServer.getGroupList(params)
      this.queryFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { groupList = [], total } = res.data
        this.tableData = groupList
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    // 删除用户组
    showDelModal(tenant) {
      const { groupId = '', groupName } = tenant
      this.$confirm(`确认删除用户组--'${groupName}'?`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          this.deleteGroup({ groupId })
        })
        .catch(() => {})
    },
    async deleteGroup(params) {
      const res = await accountManageServer.deleteGroup(params)
      console.log(res)
      if (res.code === 0) {
        this.$message.success('用户组删除成功')
        this.getGroupList()
      }
    },
    showAddTenant() {
      this.showAddModal = true
    },
    showAdminChange(row) {
      this.modifyGroupId = row.groupId
      console.log(this.modifyGroupId, 'this.modifyGroupId')
      this.showChangeModal = true
    },
    closeModal() {
      this.showAddModal = false
    },
    closeChangeModal() {
      this.showChangeModal = false
    },
    handlOK() {
      this.showAddModal = false
      this.showChangeModal = false
      this.getGroupList()
    },
    goDetail(data) {
      this.$router.push({ path: 'accountManage', query: { groupId: data.groupId } })
    }
  }
}
</script>
<style lang="less" scoped></style>
