import http from '../utils/http'

// 获取用户自定义模板配置
const querySettings = (params) => http.post('/setting/querySettings', params)
// 获取系统模板配置
const getConfig = (params) => http.get('/config/getConfig', params)
const queryAgencyMetas = (params) => http.post('/meta/agency/queryAgencyMetas', params)
// 保存配置 自定义类型
const insertSettings = (params) => http.post('/setting/insertSettings', params)
// 获取公钥
const getPub = (params) => http.get('/pub', params)
const getImageCode = (params) => http.get('/image-code', params)
export default { insertSettings, getPub, getImageCode, querySettings, getConfig, queryAgencyMetas }
