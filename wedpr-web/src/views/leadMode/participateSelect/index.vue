<template>
  <el-dialog width="1260px" height="806px" title="选择参与方" @close="handleClose" :visible="showParticipateModal">
    <div class="dataset-con">
      <el-form label-position="right" size="small" :model="dataForm" :rules="dataFormFormRules" ref="dataForm" :label-width="formLabelWidth">
        <el-form-item label="选择参与方：" prop="ownerAgencyName">
          <el-select style="width: 1010px" v-model="dataForm.ownerAgencyName" placeholder="请选择参与方">
            <el-option :label="item.label" :value="item.value" v-for="item in showSelectList" :key="item.value"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="选择数据：" prop="datasetInfo">
          <dataSelect :key="showParticipateModal" :ownerAgencyName="dataForm.ownerAgencyName" @selected="selected" />
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
// import { dataManageServer } from 'Api'
import dataSelect from '../dataSelect/index.vue'
import { mapGetters } from 'vuex'
export default {
  name: 'tagSelect',
  props: {
    showParticipateModal: {
      type: Boolean,
      default: false
    },
    ownerAgencyName: {
      type: Array,
      default: () => []
    }
  },
  components: {
    dataSelect
  },
  data() {
    return {
      dataForm: {
        ownerAgencyName: '',
        datasetInfo: null
      },

      dataFormFormRules: {
        ownerAgencyName: [{ required: true, message: '参与方不能为空', trigger: 'blur' }],
        datasetInfo: [{ required: true, message: '数据集不能为空', trigger: 'blur' }]
      },
      formLabelWidth: '112px',
      loading: false,
      groupId: '',
      pageData: { page_offset: '', page_size: '' },
      dataList: [],
      fieldsList: [],
      showSelectList: []
    }
  },
  created() {
    this.filterSelect()
  },
  computed: {
    ...mapGetters(['agencyList', 'agencyId'])
  },
  watch: {
    ownerAgencyName() {
      this.filterSelect()
    },
    showParticipateModal(nv) {
      nv && this.filterSelect()
    }
  },
  methods: {
    filterSelect() {
      const { ownerAgencyName } = this
      if (ownerAgencyName.length) {
        this.showSelectList = this.agencyList.filter((v) => ownerAgencyName.includes(v.value))
      } else {
        this.showSelectList = [...this.agencyList]
      }
      this.dataForm.ownerAgencyName = this.showSelectList.length ? this.showSelectList[0].value : ''
    },
    handleClose() {
      this.dataForm = {
        ownerAgencyName: '',
        datasetInfo: null
      }
      this.$emit('closeModal')
    },
    selected(row) {
      console.log(row)
      this.dataForm.datasetInfo = row
    },

    handleOk() {
      this.$refs.dataForm.validate((valid) => {
        if (valid) {
          const { datasetInfo = {} } = this.dataForm
          if (!datasetInfo.datasetId) {
            this.$message.error('请选择数据集')
            return
          }
          this.$emit('participateSelected', { ...datasetInfo })
        }
      })
    }
  }
}
</script>
<style lang="less" scoped>
::v-deep .el-dialog__body {
  max-height: 806px;
}
.dataset-con {
  margin-bottom: -20px;
  margin-top: -20px;
  .select-data {
    border: 1px solid #e0e4ed;
    border-radius: 4px;
    padding: 20px;
    height: auto;
    padding-bottom: 0;
  }
  .card-container {
    margin: -10px -10px;
    height: auto;
    overflow: auto;
  }

  ::v-deep div.data-card {
    margin: 10px;
    ul li:first-child {
      line-height: 26px;
      margin-bottom: 8px;
    }
    ul li span.data-size {
      i {
        font-size: 18px;
        line-height: 26px;
      }
    }
  }
}
</style>
