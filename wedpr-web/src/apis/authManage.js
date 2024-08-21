import http from '../utils/http'

// 申请授权
const createAuth = (params) => http.post('/auth/createAuth', params)
// 编辑审批
const updateAuth = (params) => http.post('/auth/updateAuth', params)
// 更新审批结果
const updateAuthResult = (params) => http.post('/auth/updateAuthResult', params)
// 关闭审批单
const closeAuthList = (params) => http.post('/auth/closeAuthList', params)
// 查询授权列表
const queryAuthList = (params) => http.post('/auth/queryAuthList', params)
// 查询关注信息
const queryFollowerAuthList = (params) => http.post('/auth/queryFollowerAuthList', params)
// 查询模板
const queryAuthTemplateDetails = (params) => http.post('/auth/template/queryAuthTemplateDetails ', params)
// 查询关注信息
const queryAuthDetail = (params) => http.get('/auth/queryAuthDetail', params)
// 查询关注信息

export default { queryAuthTemplateDetails, createAuth, updateAuth, updateAuthResult, closeAuthList, queryAuthList, queryFollowerAuthList, queryAuthDetail }
