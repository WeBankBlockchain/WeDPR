import http from '../utils/http'

// 查询投票列表
const getVotesList = (params) => http.get('/votes', params)
// 创建党工团投票
const createVoteResolution = (params) => http.post('/votes_resolution', params)
// 创建党工团投票草稿
const createVoteResolutionDraft = (params) => http.post('/votes_resolution_draft', params)
// 创建选举投票
const createVoteSelection = (params) => http.post('/votes_selection', params)
// 创建选举投票草稿
const createVoteSelectionDraft = (params) => http.post('/votes_selection_draft', params)
// 删除投票或草稿
const deleteVoteDraft = (params) => http.delete('/votes/' + params.vote_id)
// 发送投票提醒
const votesNotify = (params) => http.get('/votes_notify/' + params.vote_id, params)
// 结束投票
const votesFinish = (params) => http.get('/votes_end/' + params.vote_id, params)
// 重试结束投票
const votesReFinish = (params) => http.get('/votes_retry_count/' + params.vote_id, params)
// 查询结果
const votesResult = (params) => http.get('/votes_result/' + params.vote_id, params)
// 导出pdf结果
const votesExportResult = (params) => http.get('/votes_pdf_result/' + params.vote_id)
// 导出exlce结果
const votesExportExcleResult = (params) => http.get('/votes_excel_result/' + params.vote_id)
// 导出未投票人
const votesExportUnVoter = (params) => http.get('/votes_user_export', params)
// 审核投票
const votesAudit = (params) => http.post('/votes_audit', params)
// 审核投票列表
const votesAuditList = (params) => http.get('/votes_audit', params)
// 查看投票用户详情
const getVotesUser = (params) => http.get('/votes_user', params)
// 获取投票详情
const getVoteDetail = (params) => http.get('/votes/' + params.vote_id, params)
// 获取投票详情预览
const getVotesPreviewCode = (params) => http.get('/votes_preview/' + params.vote_id, {})

export default {
  getVotesList,
  createVoteResolution,
  createVoteResolutionDraft,
  createVoteSelection,
  createVoteSelectionDraft,
  deleteVoteDraft,
  votesNotify,
  votesFinish,
  votesResult,
  votesExportResult,
  votesAudit,
  getVotesUser,
  votesAuditList,
  getVoteDetail,
  votesExportExcleResult,
  getVotesPreviewCode,
  votesExportUnVoter,
  votesReFinish
}
