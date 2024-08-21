<template>
  <div class="create-data">
    <el-form :inline="false" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
      <formCard title="基础信息">
        <el-form-item label-width="86px" label="项目名称：" prop="template_name">
          <el-input style="width: 480px" placeholder="请输入" v-model="dataForm.sourceName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="86px" label="项目简介：" prop="vote_title_tips">
          <el-input style="width: 480px" placeholder="请输入" v-model="dataForm.des" autocomplete="off"></el-input>
        </el-form-item>
      </formCard>
      <formCard title="选择发布数据">
        <dataCard :showEdit="false" showSelect v-for="i in [1, 2, 3, 4, 5, 6, 7]" :key="i" />
        <we-pagination
          :pageSizesOption="[8, 12, 16, 24, 32]"
          :total="total"
          :page_offset="pageData.page_offset"
          :page_size="pageData.page_size"
          @paginationChange="paginationHandle"
        ></we-pagination>
      </formCard>
      <formCard title="设置发布范围">
        <el-form-item label-width="86px" label="设置范围：" prop="setting">
          <el-checkbox-group v-model="dataForm.setting">
            <el-checkbox label="本部分内" name="type"></el-checkbox>
            <el-checkbox label="本机构内" name="type1"></el-checkbox>
            <el-checkbox name="type2"
              >指定机构
              <el-input
                size="small"
                style="width: 400px; margin-left: 16px"
                placeholder="指定机构请输入机构编号，多个机构编号用“,”隔开"
                v-model="dataForm.tags"
                autocomplete="off"
              ></el-input>
            </el-checkbox>
            <el-checkbox name="type3"
              >指定用户
              <el-input
                size="small"
                style="width: 400px; margin-left: 16px"
                placeholder="指定用户请输入用户名，多个用户名用“,”隔开"
                v-model="dataForm.tags"
                autocomplete="off"
              ></el-input
            ></el-checkbox>
            <el-checkbox label="全局" name="type4"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </formCard>
      <formCard title="设置查询规则" v-if="type === 'PIR'">
        <el-form-item label-width="100px" label="查询存在性：" prop="template_name">
          <el-input style="width: 480px" placeholder="请输入" v-model="dataForm.sourceName" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label-width="100px" label="查询字段值：" prop="vote_title_tips">
          <el-input style="width: 480px" placeholder="请输入" v-model="dataForm.des" autocomplete="off"></el-input>
        </el-form-item>
      </formCard>
    </el-form>
    <div>
      <el-button size="medium" :loading="queryFlag" @click="queryHandle"> 上一步 </el-button>
      <el-button size="medium" type="primary" @click="createAccount"> 下一步 </el-button>
    </div>
  </div>
</template>
<script>
import formCard from '@/components/formCard.vue'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import dataCard from '@/components/dataCard.vue'
export default {
  name: 'serverCreate',
  mixins: [tableHeightHandle],
  components: {
    formCard,
    dataCard
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
        page_size: 12
      },
      total: -1,
      queryFlag: false,
      tableData: [],
      loadingFlag: false,
      showAddModal: false,
      dataForm: {
        setting: []
      }
    }
  },
  created() {
    const { type } = this.$router.query
    this.type = type
  },
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
