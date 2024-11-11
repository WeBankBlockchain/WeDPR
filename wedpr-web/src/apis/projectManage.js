import http from '../utils/http'

// 创建项目
const createProject = (params) => http.post('/project/createProject', params)
// 更新项目
const updateProject = (params) => http.post('/project/updateProject', params)
// 查询列表
const queryProject = (params) => http.post('/project/queryProjectByCondition', params)
// 删除项目
const deleteProject = (params) => http.post('/project/deleteProject', params)
// 提交任务
const submitJob = (params) => http.post('/project/submitJob', params)

const queryJobOverview = (params) => http.post('/project/queryJobOverview', params)
// 审计列表
const queryRecordSyncStatus = (params) => http.post('/sync/queryRecordSyncStatus', params)
// 数据集关联任务列表
const queryJobsByDatasetID = (params) => http.get('/project/queryJobsByDatasetID', params)

export default {
  createProject,
  queryProject,
  updateProject,
  deleteProject,
  submitJob,
  queryRecordSyncStatus,
  queryJobOverview,
  queryJobsByDatasetID
}
