import http from '../utils/http'

// 查询模板列表
const getMsgList = (params) => http.get('/msgList', params)

export default { getMsgList }
