<template>
  <div class="access-manage" style="position: relative; height: 100%">
    <div class="warning">
      <p><i class="el-icon-warning"></i> AccessKeyID 和 AccessKey Secret 是您访问该隐私计算平台 API的密钥，具有该账户完全的权限，请您妥善保管。</p>
      <p class="next">AccessKey在线时间越长，泄露风险越高。您应定期创建新 AccessKey 替代旧的。</p>
    </div>
    <div class="form-search">
      <el-form :inline="true" @submit="queryHandle" :model="searchForm" ref="searchForm" size="small">
        <el-form-item>
          <el-button type="primary" icon="el-icon-plus" @click="showAddKey"> 创建AccessKey </el-button>
        </el-form-item>
        <div style="float: right">
          <el-form-item label-width="124px" label="AccesssKey ID：" prop="accessKeyID">
            <el-input style="width: 160px" placeholder="请输入" v-model="searchForm.accessKeyID" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item prop="status" label="状态：">
            <el-select style="width: 160px" v-model="searchForm.status" placeholder="请选择" clearable>
              <el-option label="已启用" value="Enable"></el-option>
              <el-option label="已禁用" value="Disable"></el-option>
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
        <el-table-column label="AccessKey ID" prop="accessKeyID" show-overflow-tooltip />
        <el-table-column label="状态" prop="roleName" show-overflow-tooltip>
          <template v-slot="scope">
            <el-tag size="small" v-if="scope.row.status === 'Enable'" effect="dark" color="#52B81F">已启用</el-tag>
            <el-tag size="small" v-if="scope.row.status === 'Disable'" effect="dark" color="#FF4D4F">已禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="描述" prop="desc" show-overflow-tooltip />
        <el-table-column label="创建时间" prop="createTime" show-overflow-tooltip />
        <el-table-column label="已创建时间" prop="lastUpdateTime" show-overflow-tooltip />
        <el-table-column label="操作" width="180px">
          <template v-slot="scope">
            <el-button v-if="scope.row.status === 'Disable'" @click="setAccessKeyStatus(scope.row, 'Enable')" size="small" type="text">启用</el-button>
            <el-button v-if="scope.row.status === 'Enable'" @click="setAccessKeyStatus(scope.row, 'Disable')" size="small" type="text">禁用</el-button>
            <el-button @click="openCopyModal(scope.row)" size="small" type="text">复制</el-button>
          </template>
        </el-table-column>
      </el-table>
      <we-pagination :total="total" :page_offset="pageData.page_offset" :page_size="pageData.page_size" @paginationChange="paginationHandle"></we-pagination>
    </div>
    <el-empty v-if="!total" :image-size="120" desccription="暂无数据">
      <img slot="image" src="~Assets/images/pic_empty_news.png" alt="" />
    </el-empty>
    <el-dialog width="400px" title="创建AccessKey" @close="handleClose" :visible="showAddModal">
      <div class="form-con">
        <el-form label-position="right" size="small" :model="accessForm" :rules="accessFormRules" ref="accessForm" label-width="96px">
          <el-form-item label="描述信息：" prop="desc">
            <el-input size="small" style="width: 260px" placeholder="请输入描述信息" v-model="accessForm.desc" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="handleClose">取 消</el-button>
        <el-button size="small" :loading="loading" type="primary" @click="handleOk">确 定</el-button>
      </div>
    </el-dialog>
    <el-dialog width="660px" title="复制AccessKey" @close="handleCopyClose" :visible="showCopyModal">
      <div class="form-con">
        <div class="warning">
          <p><i class="el-icon-warning copy-warning"></i>请妥善保管，谨防泄露</p>
        </div>
        <div class="key-info">
          <ul>
            <li>AccessID：</li>
            <li>{{ accessInfo.accessKeyID }}</li>
          </ul>
          <ul>
            <li>Access Secret：</li>
            <li>
              {{ showSercret ? accessInfo.accessKeySecret : maskString(accessInfo.accessKeySecret) }}
              <img v-if="showSercret" @click="hideSecretStr" src="~Assets/images/hide.png" alt="" />
              <img @click="openSecretStr" v-else src="~Assets/images/show.png" alt="" />
            </li>
          </ul>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button size="small" @click="handleCopyClose">取 消</el-button>
        <el-button size="small" :loading="loading" type="primary" @click="handleCopy">复制</el-button>
      </div>
    </el-dialog>
  </div>
</template>
<script>
import { accessKeyManageServer } from 'Api'
import { tableHeightHandle } from 'Mixin/tableHeightHandle.js'
import { handleParamsValid, maskString } from 'Utils/index.js'
export default {
  name: 'accessKeyManage',
  mixins: [tableHeightHandle],
  data() {
    return {
      searchForm: {
        accessKeyID: '',
        status: ''
      },
      searchQuery: {
        accessKeyID: '',
        status: ''
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
      accessForm: {
        desc: ''
      },
      accessFormRules: {
        desc: [{ required: true, message: '描述信息不能为空', trigger: 'blur' }]
      },
      loading: false,
      showCopyModal: false,
      accessInfo: {
        accessKeyID: '',
        accessKeySecret: ''
      },
      showSercret: false,
      maskString
    }
  },
  created() {
    this.queryAccessKeyList()
  },
  methods: {
    showAddKey() {
      this.showAddModal = true
    },
    hideSecretStr() {
      this.showSercret = false
    },
    openSecretStr() {
      this.showSercret = true
    },
    openCopyModal(accessData) {
      this.showCopyModal = true
      const { accessKeyID, accessKeySecret } = accessData
      this.accessInfo = {
        accessKeyID,
        accessKeySecret
      }
    },
    handleCopy() {
      const { accessKeyID, accessKeySecret } = this.accessInfo
      this.copy(accessKeyID, accessKeySecret)
    },
    copy(accessKeyID, accessKeySecret) {
      // 模拟 输入框
      const val = `accessKeyID：${accessKeyID}\naccessKeySecret：${accessKeySecret}`
      const cInput = document.createElement('textarea')
      cInput.value = val
      document.body.appendChild(cInput)
      cInput.select() // 选取文本框内容
      // 执行浏览器复制命令
      // 复制命令会将当前选中的内容复制到剪切板中（这里就是创建的input标签）
      // Input要在正常的编辑状态下原生复制方法才会生效
      document.execCommand('copy')
      this.$message.success('内容已复制到剪贴板')
      // 复制成功后再将构造的标签 移除
      document.body.removeChild(cInput)
    },
    // 查询
    queryHandle() {
      this.$refs.searchForm.validate((valid) => {
        if (valid) {
          this.searchQuery = { ...this.searchForm }
          this.pageData.page_offset = 1
          this.queryAccessKeyList()
        } else {
          return false
        }
      })
    },
    reset() {
      this.$refs.searchForm.resetFields()
    },
    handleOk() {
      this.loading = true
      this.$refs.accessForm.validate((valid) => {
        if (valid) {
          this.genAccessKey({ ...this.accessForm })
        } else {
          this.loading = false
        }
      })
    },
    handleClose() {
      this.showAddModal = false
    },
    handleCopyClose() {
      this.showCopyModal = false
    },
    // 分页切换
    paginationHandle(pageData) {
      console.log(pageData, 'pagData')
      this.pageData = { ...pageData }
      this.queryAccessKeyList()
    },
    // 获取ak列表
    async queryAccessKeyList() {
      const { page_offset, page_size } = this.pageData
      const { accessKeyID, status } = this.searchQuery
      let params = handleParamsValid({ accessKeyID, status })
      params = { condition: { ...params, id: '' }, pageNum: page_offset, pageSize: page_size }
      this.loadingFlag = true
      const res = await accessKeyManageServer.queryAccessKeyList(params)
      this.loadingFlag = false
      console.log(res)
      if (res.code === 0 && res.data) {
        const { credentials = [], total } = res.data
        this.tableData = credentials
        this.total = total
      } else {
        this.tableData = []
        this.total = 0
      }
    },
    async setAccessKeyStatus(params, status) {
      const { id } = params
      const res = await accessKeyManageServer.updateAccessKey({ id, status })
      console.log(res)
      if (res.code === 0) {
        this.$message.success('凭证' + (status === 'Enable' ? '启用' : '禁用') + '成功')
        this.queryAccessKeyList()
      }
    },
    async genAccessKey(params) {
      const res = await accessKeyManageServer.genAccessKey(params)
      this.loading = false
      if (res.code === 0) {
        this.$message.success('凭证生成成功')
        this.queryAccessKeyList()
        this.showAddModal = false
      }
    }
  }
}
</script>
<style lang="less" scoped>
div.access-manage {
  ::v-deep .el-tag {
    padding: 0 12px;
    border: none;
    line-height: 24px;
  }
  ::v-deep .el-dialog__body {
    padding-bottom: 12px;
  }
  div.warning {
    border: 1px solid #e6a23c;
    background-color: #fdf6ec;
    padding: 12px;
    margin-bottom: 30px;
    border-radius: 4px;
    p {
      line-height: 24px;
    }
    p.next {
      text-indent: 20px;
    }
    i {
      color: #e6a23c;
      font-size: 16px;
    }
  }
  i.copy-warning {
    color: #e6a23c;
    font-size: 16px;
    line-height: 24px;
    margin-right: 6px;
  }
  .key-info {
    background-color: rgb(246, 247, 251);
    padding: 20px 10px;

    ul {
      display: flex;

      li:first-child {
        text-align: right;
        width: 120px;
      }
      li:last-child {
        flex: 1;
        img {
          vertical-align: middle;
          width: 20px;
          height: auto;
          margin-left: 10px;
          cursor: pointer;
        }
      }
    }
    ul:first-child {
      margin-bottom: 10px;
    }
  }
}
</style>
