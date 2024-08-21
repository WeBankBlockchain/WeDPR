import http from '../utils/http'

// 查任务
const queryJobByCondition = (params) => http.post('/project/queryJobByCondition', params)
// 查询详情
const queryProject = (params) => http.post('/project/queryProjectByCondition', params)
// 删除项目
const deleteProject = (params) => http.post('/project/deleteProject', params)
// 查询关注任务
const queryFollowerJobByCondition = (params) => http.post('/project/queryFollowerJobByCondition', params)
// 查询任务进展
const queryJobDetail = (params) => http.get('/scheduler/queryJobDetail', params)

export default { queryJobByCondition, queryProject, deleteProject, queryFollowerJobByCondition, queryJobDetail }
