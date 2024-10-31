export const approveStatusMap = {
  ToConfirm: '待确认',
  Approving: '审批中',
  ApproveFailed: '失败',
  ApproveRejected: '驳回',
  ApproveCanceled: '废弃',
  ApproveSuccess: '审批完成',
  ProgressFailed: '失败',
  Progressing: '生效中',
  ProgressSuccess: '已生效'
}
export const jobStatusMap = {
  Submitted: '已提交',
  Handshaking: '握手中',
  HandshakeSuccess: '握手成功',
  HandshakeFailed: '握手失败',
  Running: '运行中',
  RunFailed: '运行失败',
  RunSuccess: '运行成功',
  WaitToRetry: '等待重试',
  WaitToKill: '等待被终止',
  Killing: '正在终止',
  Killed: '已终止',
  KillFailed: '终止失败',
  ChainInProgress: '上链中'
}
export const opType = {
  Authorization: '审批',
  Job: '任务',
  Dataset: '数据集'
}
export const actionMap = {
  RunAction: '执行任务',
  CreateDataset: '创建数据集',
  CreateAuth: '创建审批单',
  UpdateAuth: '更新审批单',
  CreateAuthTemplates: '创建审批模板',
  UpdateAuthTemplates: '更新审批模板',
  DeleteAuthTemplates: '删除审批模板',
  RemoveDataset: '删除数据集',
  UpdateDataset: '更新数据集'
}
export const dataActionMap = {
  CreateDataset: '创建数据集',
  RemoveDataset: '删除数据集',
  UpdateDataset: '更新数据集'
}
export const approveActionMap = {
  CreateAuth: '创建审批单',
  UpdateAuth: '更新审批单',
  CreateAuthTemplates: '创建审批模板',
  UpdateAuthTemplates: '更新审批模板',
  DeleteAuthTemplates: '删除审批模板'
}
export const jobActionMap = {
  RunAction: '执行任务'
}

export const actionStatus = {
  WaitingSubmitToChain: '待上链',
  SubmittedToChain: '上链中',
  SubmittedToChainFailed: '上链失败',
  SubmittedToChainSuccess: '上链成功',
  CommitSuccess: '操作成功',
  CommitFailed: '操作失败'
}

export const actionScreenStatus = {
  WaitingSubmitToChain: '待上链',
  SubmittedToChain: '上链中',
  SubmittedToChainFailed: '上链失败',
  SubmittedToChainSuccess: '上链成功',
  CommitSuccess: '成功',
  CommitFailed: '失败'
}

export const dataStatusEnum = {
  Success: 0,
  Failure: -1,
  Fatal: -2,
  Created: 1
}
export const jobEnum = {
  XGB_TRAINING: 'XGB_TRAINING',
  XGB_PREDICTING: 'XGB_PREDICTING',
  LR_TRAINING: 'LR_TRAINING',
  LR_PREDICTING: 'LR_PREDICTING',
  PSI: 'PSI',
  SQL: 'SQL',
  PIR: 'PIR',
  MPC: 'MPC'
}

export const settingMap = {
  XGB_TRAINING: 'XGB_SETTING',
  LR_TRAINING: 'LR_SETTING'
}

// 任务生成的模型
export const modelSettingMap = {
  LR_TRAINING: 'LR_MODEL_SETTING',
  XGB_TRAINING: 'XGB_MODEL_SETTING'
}
// 创建任务所需模型
export const jobModelSettingMap = {
  LR_PREDICTING: 'LR_MODEL_SETTING',
  XGB_PREDICTING: 'XGB_MODEL_SETTING'
}

export const serviceTypeEnum = {
  PIR: 'pir',
  XGB: 'xgb',
  LR: 'lr'
}
export const serviceAuthStatus = {
  Owner: 'Owner',
  Authorized: 'Authorized',
  NoPermission: 'NoPermission',
  Expired: 'Expired'
}
export const servicePulishStatus = {
  Publishing: '发布中',
  PublishSuccess: '发布成功',
  PublishFailed: '发布失败'
}

export const searchTypeEnum = {
  SearchExist: 'SearchExist',
  SearchValue: 'SearchValue',
  ALL: 'ALL'
}

export const searchTypeDesEnum = {
  SearchExist: '查询存在性',
  SearchValue: '查询字段值',
  ALL: '查询存在性，查询字段值 '
}

export const agencyStatusEnum = {
  OPEN: 0,
  CLOSE: 1
}
export const certStatusEnum = {
  NO_CERT: 0,
  OPEND: 1,
  OUTDATE: 2,
  CLOSED: 3
}
export const certStatusMap = {
  0: '无证书',
  1: '有效',
  2: '过期',
  3: '禁用'
}
export const certUseStatusEnum = {
  OPEN: 0,
  CLOSE: 1
}
export const certUseStatusMap = {
  1: '禁用',
  0: '启用'
}

export function mapToList(mapObject) {
  const data = []
  Object.keys(mapObject).forEach((key) => {
    data.push({ value: key, label: mapObject[key] })
  })
  return data
}
// const data = {
//   version: '1.0',
//   templates: [
//     { name: 'PSI', title: '数据对齐', participateNumber: '2+', detail: '', version: '1.0' },
//     { name: 'PIR', title: '匿踪查询', participateNumber: '1', detail: '', version: '1.0' },
//     { name: 'SQL', title: '连表分析', participateNumber: '3+', detail: '', version: '1.0' },
//     { name: 'MPC', title: '自定义计算', participateNumber: '3+', detail: '', version: '1.0' },
//     { name: 'XGB_TRAINING', title: 'SecureLGBM训练', participateNumber: '1+', needTagsProvider: true, detail: '', version: '1.0' },
//     { name: 'XGB_PREDICTING', title: 'SecureLGBM预测', participateNumber: '2+', detail: '', version: '1.0' },
//     { name: 'LR_TRAINING', title: 'SecureLR建模', participateNumber: '1+', needTagsProvider: true, detail: '', version: '1.0' },
//     { name: 'LR_PREDICTING', title: 'SecureLR预测', participateNumber: '2+', value: 'LR_PREDICTING', participateNumber: '2+', detail: '', version: '1.0' }
//   ]
// }
// export const algListFull = [
//   {
//     label: '数据对齐',
//     value: 'PSI',
//     participateNumber: '2+'
//   },
//   {
//     label: 'SecureLGBM训练',
//     value: 'XGB_TRAINING',
//     participateNumber: '1+',
//     needTagsProvider: true
//   },
//   {
//     label: 'SecureLGBM预测',
//     value: 'XGB_PREDICTING',
//     participateNumber: '2+'
//   },
//   {
//     label: '连表SQL分析',
//     value: 'SQL',
//     participateNumber: '3+'
//   },
//   {
//     label: '自定义计算',
//     value: 'MPC',
//     participateNumber: '3+'
//   },
//   {
//     label: '匿踪查询',
//     value: 'PIR',
//     participateNumber: '1'
//   },
//   {
//     label: 'SecureLR训练',
//     value: 'LR_TRAINING',
//     participateNumber: '1+',
//     needTagsProvider: true
//   },
//   {
//     label: 'SecureLR预测',
//     value: 'LR_PREDICTING',
//     participateNumber: '2+'
//   }
// ]
export const upTypeList = [
  {
    label: 'CSV文件',
    value: 'CSV'
  },
  {
    label: 'EXCEL文件',
    value: 'EXCEL'
  },
  {
    label: '数据库',
    value: 'DB'
  },
  {
    label: 'HIVE',
    value: 'HIVE'
  },
  {
    label: 'HDFS',
    value: 'HDFS'
  }
]

export const jobStatusList = mapToList(jobStatusMap)
export const approveStatusList = mapToList(approveStatusMap).filter((v) => v.value !== 'ProgressFailed')
export const opTypeList = mapToList(opType)
export const actionStatusList = mapToList(actionStatus)
export const actionMapList = mapToList(actionMap)
export const dataActionMapList = mapToList(dataActionMap)
export const approveActionMapList = mapToList(approveActionMap)
export const jobActionMapList = mapToList(jobActionMap)
export const certStatusMapList = mapToList(certStatusMap)
export const certUseMapList = mapToList(certUseStatusMap)
export const servicePulishStatusList = mapToList(servicePulishStatus)
