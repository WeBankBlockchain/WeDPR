export const approveStatusMap = {
  ToConfirm: '待确认',
  Approving: '审批中',
  ApproveFailed: '失败',
  ApproveRejected: '驳回',
  ApproveCanceled: '作废',
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
  WaitToKill: '等待被kill',
  Killing: 'kill中',
  Killed: '已经被kill'
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

export const jobEnum = {
  XGB_TRAINING: 'XGB_TRAINING',
  XGB_PREDICTING: 'XGB_PREDICTING',
  PSI: 'PSI'
}
export const certStatusMap = {
  0: '无证书',
  1: '有效',
  2: '过期',
  3: '禁用'
}

export function mapToList(mapObject) {
  const data = []
  Object.keys(mapObject).forEach((key) => {
    data.push({ value: key, label: mapObject[key] })
  })
  return data
}
export const algListFull = [
  {
    label: '隐私求交',
    value: 'PSI',
    src: require('../assets/images/psi_job.png'),
    jobSrc: require('../assets/images/PSI.png')
  },
  {
    label: 'SecureLGBM训练',
    value: 'XGB_TRAINING',
    src: require('../assets/images/xgbtrain_job.png'),
    jobSrc: require('../assets/images/XGB.png')
  },
  {
    label: 'SecureLGBM预测',
    value: 'XGB_PREDICTING',
    src: require('../assets/images/xgbpredict_job.png'),
    jobSrc: require('../assets/images/XGB_2.png')
  },
  {
    label: '连表SQL分析',
    value: 'SQL',
    src: require('../assets/images/SQL.png'),
    jobSrc: require('../assets/images/SQL.png')
  },
  // {
  //   label: '多方XGB建模',
  //   value: 'XGB_MUL',
  //   src: require('../assets/images/XGB_MUL.png'),
  //   jobSrc: require('../assets/images/XGB_MUL.png')
  // },
  {
    label: '两方LR建模',
    value: 'LR_2',
    src: require('../assets/images/LR_2.png'),
    jobSrc: require('../assets/images/LR_2.png')
  },
  {
    label: '多方LR建模',
    value: 'LR_MUL',
    src: require('../assets/images/LR_MUL.png'),
    jobSrc: require('../assets/images/LR_MUL.png')
  },
  // {
  //   label: '多方XGB预测',
  //   value: 'XGB_PREDICT_MUL',
  //   src: require('../assets/images/XGB_PREDICT_MUL.png'),
  //   jobSrc: require('../assets/images/XGB_PREDICT_MUL.png')
  // },
  {
    label: '两方LR预测',
    value: 'LR',
    src: require('../assets/images/LR.png'),
    jobSrc: require('../assets/images/LR.png')
  },
  {
    label: '多方LR预测',
    value: 'LR_PREDICT_MUL',
    src: require('../assets/images/LR_PREDICT_MUL.png'),
    jobSrc: require('../assets/images/LR_PREDICT_MUL.png')
  }
]
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
