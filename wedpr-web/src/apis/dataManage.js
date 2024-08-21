import http from '../utils/http'

// 获取上传方式下拉
const getDataUploadType = (params) => http.get('/dataset/getDataUploadType', params)
// 上传分片
const uploadChunkData = (params) => http.post('/dataset/uploadChunkData', params)
// 合并分片
const mergeChunkData = (params) => http.post('/dataset/mergeChunkData', params)
// 删除群组
const createDataset = (params) => http.post('/dataset/createDataset', params)
// 更新数据集
const updateDataset = (params) => http.post('/dataset/updateDataset', params)
// 删除数据集
const deleteDataset = (params) => http.post('/dataset/deleteDataset', params)
// 删除数据集列表
const deleteDatasetList = (params) => http.post('/dataset/deleteDatasetList', params)
// 获取数据集
const listDataset = (params) => http.get('/dataset/listDataset', params)
// 根据ID获取数据集list
const queryDatasetList = (params) => http.post('/dataset/queryDatasetList', params)
// 获取数据集
const queryDataset = (params) => http.get('/dataset/queryDataset', params)
// 获取数据集
const getFileShardsInfo = (params) => http.get('/dataset/getFileShardsInfo', params)
const downloadFileShardData = (params) => http.postStream('/dataset/downloadFileShardData', params)

export default {
  downloadFileShardData,
  getFileShardsInfo,
  deleteDatasetList,
  queryDatasetList,
  getDataUploadType,
  uploadChunkData,
  mergeChunkData,
  createDataset,
  updateDataset,
  deleteDataset,
  listDataset,
  queryDataset
}
