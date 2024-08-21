import http from '../utils/http'

// 查询明细
const getLogList = (params) => http.get('/logList', params)

export default { getLogList }
