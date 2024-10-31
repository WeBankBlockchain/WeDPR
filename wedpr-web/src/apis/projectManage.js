import http from '../utils/http'

// 创建项目
const createProject = (params) => http.post('/project/createProject', params)
// 更新项目
const updateProject = (params) => http.post('/project/updateProject', params)
// 查询列表
const queryProject = (params) => http.post('/project/queryProjectByCondition', params)
// 管理员查询列表
const adminQueryProject = (params) => http.get('/admin/listProject', params)
// 管理员查询项目内任务列表
const adminQuerylistJobInProject = (params) => http.get('/admin/listJob', params)
// 管理员查询项目内任务列表
const adminQueryJobsByDatasetId = (params) => http.get('/admin/queryJobsByDatasetId', params)
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
  adminQuerylistJobInProject,
  adminQueryProject,
  createProject,
  queryProject,
  updateProject,
  deleteProject,
  submitJob,
  queryRecordSyncStatus,
  queryJobOverview,
  queryJobsByDatasetID,
  adminQueryJobsByDatasetId
}
