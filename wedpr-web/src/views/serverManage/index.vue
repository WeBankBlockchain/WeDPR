<template>
  <div class="group-manage">
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item prop="username" label="发布用户组：">
          <el-select v-model="searchForm.role_name" placeholder="请输入" clearable>
            <el-option label="系统管理员" value="系统管理员"></el-option>
            <el-option label="投票管理员" value="投票管理员"> </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="role_name" label="发布用户：">
          <el-select v-model="searchForm.role_name" placeholder="请输入" clearable>
            <el-option label="系统管理员" value="系统管理员"></el-option>
            <el-option label="投票管理员" value="投票管理员"> </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="role_name" label="服务名称：">
          <el-select v-model="searchForm.role_name" placeholder="请输入" clearable>
            <el-option label="系统管理员" value="系统管理员"></el-option>
            <el-option label="投票管理员" value="投票管理员"> </el-option>
          </el-select>
        </el-form-item>
        <el-form-item prop="role_name" label="发布时间：">
          <el-date-picker style="width: 160px" v-model="value1" type="date" placeholder="请选择日期"> </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="queryFlag" @click="queryHandle">
            {{ queryFlag ? '查询中...' : '查询' }}
          </el-button>
        </el-form-item>
        <div class="op-container">
          <div class="btn" @click="creatPsi"><img class="icon-btn" src="~Assets/images/lead icon_service1.png" alt="" /> 发布匿踪查询服务</div>
          <div class="btn model" @click="creatModel"><img class="icon-btn" src="~Assets/images/lead icon_service2.png" alt="" /> 发布模型预测服务</div>
        </div>
      </el-form>
    </div>
    <div class="card-container">
      <div class="server-con" v-for="item in [1, 23, 4, 5, 6, 77, 8]" :key="item">
        <img src="~Assets/images/icon_service1.png" alt="" />
        <dl>
          <dt>匿踪查询服务</dt>
          <dd>数据资源数量：<span class="count">3</span></dd>
          <dd>机构数量：<span class="count">3</span></dd>
          <dd>发布人：<span>zhangsan</span></dd>
        </dl>
      </div>
    </div>
    <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
  </div>
</template>
<script>
import wePagination from '@/components/wePagination.vue'
export default {
  name: 'serverManage',
  components: {
    wePagination
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
      total: 10,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false
    }
  },
  created() {},
  methods: {
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
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.getAccountList()
    },
    creatPsi() {
      this.$router.push({ path: '/serverCreate', query: { type: 'PIR' } })
    },
    creatModel() {
      this.$router.push({ path: '/serverCreate', query: { type: 'MODEL' } })
    }
  }
}
</script>
<style lang="less" scoped>
div.op-container {
  margin: 16px 0;
  div.btn {
    padding: 5px 16px;
    border-radius: 4px;
    cursor: pointer;
    background: #51a14e;
    color: white;
    display: inline-block;
    font-size: 14px;
    line-height: 22px;
    margin-right: 12px;
  }
  div.btn:hover {
    opacity: 0.8;
  }
  div.model {
    background: #0ca8ff;
  }
  img.icon-btn {
    width: 16px;
    height: 16px;
    transform: translateY(2px);
    margin-right: 9px;
  }
}
div.card-container {
  margin-left: -16px;
  margin-right: -16px;
  overflow: hidden;
}
div.server-con {
  border: 1px solid #e0e4ed;
  padding: 20px;
  box-sizing: border-box;
  border-radius: 12px;
  float: left;
  width: calc(25% - 32px);
  background-color: #f6f8fc;
  margin: 20px 16px;
  cursor: pointer;
  min-width: 240px;
  img {
    width: 48px;
    height: 48px;
    margin-bottom: 16px;
  }
  dl {
    dt {
      font-size: 16px;
      line-height: 24px;
      color: #262a32;
      font-weight: bolder;
      margin-bottom: 16px;
    }
    dd {
      font-size: 12px;
      line-height: 24px;
      margin-bottom: 4px;
      color: #787b84;
      span {
        float: right;
      }
      span.count {
        font-size: 16px;
        color: #262a32;
        font-weight: 500;
      }
    }
  }
}
div.server-con:hover {
  box-shadow: 0px 2px 10px 2px #00000014;
  cursor: pointer;
}
</style>
