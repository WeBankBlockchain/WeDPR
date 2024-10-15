import json

common_template = [{
    'label': 'use_psi',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否进行隐私求交,只能为0或1'
}, {
    'label': 'fillna',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否进行缺失值填充,只能为0或1'
}, {
    'label': 'na_select',
    'type': 'float',
    'value': 1,
    'min_value': 0,
    'max_value': 1,
    'description': '缺失值筛选阈值,取值范围为0~1之间(0表示只要有缺失值就移除,1表示移除全为缺失值的列)'
}, {
    'label': 'filloutlier',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否进行异常值处理,只能为0或1'
}, {
    'label': 'normalized',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否归一化,只能为0或1'
}, {
    'label': 'standardized',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否标准化,只能为0或1'
}, {
    'label': 'categorical',
    'type': 'string',
    'value': '',
    'description': '标记所有分类特征字段,格式:x1,x12(空代表无分类特征)'
}, {
    'label': 'psi_select_col',
    'type': 'string',
    'value': '',
    'description': 'PSI稳定性筛选时间列名(空代表不进行PSI筛选)'
}, {
    'label': 'psi_select_base',
    'type': 'string',
    'value': '',
    'description': 'PSI稳定性筛选的基期(空代表不进行PSI筛选)'
}, {
    'label': 'psi_select_thresh',
    'type': 'float',
    'value': 0.3,
    'min_value': 0,
    'max_value': 1,
    'description': 'PSI筛选阈值,取值范围为0~1之间'
}, {
    'label': 'psi_select_bins',
    'type': 'int',
    'value': 4,
    'min_value': 3,
    'max_value': 100,
    'description': '计算PSI时分箱数,取值范围为3~100之间'
}, {
    'label': 'corr_select',
    'type': 'float',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '特征相关性筛选阈值,取值范围为0~1之间(值为0时不进行相关性筛选)'
}, {
    'label': 'use_iv',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否使用iv进行特征筛选, 只能为0或1'
}, {
    'label': 'group_num',
    'type': 'int',
    'value': 4,
    'min_value': 3,
    'max_value': 100,
    'description': 'woe计算分箱数,取值范围为3~100之间的整数'
}, {
    'label': 'iv_thresh',
    'type': 'float',
    'value': 0.1,
    'min_value': 0.01,
    'max_value': 1,
    'description': 'iv特征筛选的阈值,取值范围为0.01~1之间'
}, {
    'label': 'use_goss',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '是否采用goss抽样加速训练, 只能为0或1'
}, {
    'label': 'silent',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '值为1时不打印运行信息,只能为0或1'
}, {
    'label': 'verbose_eval',
    'type': 'int',
    'value': 1,
    'min_value': 0,
    'max_value': 100,
    'description': '按传入的间隔输出训练过程中的评估信息,0表示不打印'
}, {
    'label': 'eval_set_column',
    'type': 'string',
    'value': '',
    'description': '指定训练集测试集标记字段名称'
}, {
    'label': 'train_set_value',
    'type': 'string',
    'value': '',
    'description': '指定训练集标记值'
}, {
    'label': 'eval_set_value',
    'type': 'string',
    'value': '',
    'description': '指定测试集标记值'
}, {
    'label': 'train_features',
    'type': 'string',
    'value': '',
    'description': '指定入模特征'
},
    {
    'label': 'learning_rate',
        'type': 'float',
        'value': 0.1,
        'min_value': 0.01,
        'max_value': 1,
        'description': '学习率, 取值范围为0.01~1之间'
},
    {
        'label': 'random_state',
        'type': 'str',
        'value': "",
        'description': '随机数种子'
}
]


xgb_model_template = [{
    'label': 'test_dataset_percentage',
    'type': 'float',
    'value': 0.3,
    'min_value': 0.1,
    'max_value': 0.5,
    'description': '测试集比例, 取值范围为0.1~0.5之间'
}, {
    'label': 'num_trees',
    'type': 'int',
    'value': 6,
    'min_value': 1,
    'max_value': 300,
    'description': 'XGBoost迭代树棵树, 取值范围为1~300之间的整数'
}, {
    'label': 'max_depth',
    'type': 'int',
    'value': 3,
    'min_value': 1,
    'max_value': 6,
    'description': 'XGBoost树深度, 取值范围为1~6之间的整数'
}, {
    'label': 'max_bin',
    'type': 'int',
    'value': 4,
    'min_value': 3,
    'max_value': 100,
    'description': '特征分箱数, 取值范围为3~100之间的整数'
}, {
    'label': 'silent',
    'type': 'bool',
    'value': 0,
    'min_value': 0,
    'max_value': 1,
    'description': '值为1时不打印运行信息,只能为0或1'
}, {
    'label': 'subsample',
    'type': 'float',
    'value': 1,
    'min_value': 0.1,
    'max_value': 1,
    'description': '训练每棵树使用的样本比例,取值范围为0.1~1之间'
}, {
    'label': 'colsample_bytree',
    'type': 'float',
    'value': 1,
    'min_value': 0.1,
    'max_value': 1,
    'description': '训练每棵树使用的特征比例,取值范围为0.1~1之间'
}, {
    'label': 'colsample_bylevel',
    'type': 'float',
    'value': 1,
    'min_value': 0.1,
    'max_value': 1,
    'description': '训练每一层使用的特征比例,取值范围为0.1~1之间'
}, {
    'label': 'reg_alpha',
    'type': 'float',
    'value': 0,
    'min_value': 0,
    'description': 'L1正则化项,用于控制模型复杂度,取值范围为大于等于0的数值'
}, {
    'label': 'reg_lambda',
    'type': 'float',
    'value': 1,
    'min_value': 0,
    'description': 'L2正则化项,用于控制模型复杂度,取值范围为大于等于0的数值'
}, {
    'label': 'gamma',
    'type': 'float',
    'value': 0,
    'min_value': 0,
    'description': '最优分割点所需的最小损失函数下降值,取值范围为大于等于0的数值'
}, {
    'label': 'min_child_weight',
    'type': 'float',
    'value': 0,
    'min_value': 0,
    'description': '最优分割点所需的最小叶子节点权重,取值范围为大于等于0的数值'
}, {
    'label': 'min_child_samples',
    'type': 'int',
    'value': 10,
    'min_value': 1,
    'max_value': 1000,
    'description': '最优分割点所需的最小叶子节点样本数量,取值范围为1~1000之间的整数'
}, {
    'label': 'seed',
    'type': 'int',
    'value': 2024,
    'min_value': 0,
    'max_value': 10000,
    'description': '分割训练集测试集时随机数种子,取值范围为0~10000之间的整数'
}, {
    'label': 'early_stopping_rounds',
    'type': 'int',
    'value': 0,
    'min_value': 0,
    'max_value': 100,
    'description': '指定迭代多少次没有提升则停止训练, 值为0时不执行, 取值范围为0~100之间的整数'
}, {
    'label': 'eval_metric',
    'type': 'string',
    'value': 'auc',
    'description': '早停的评估指标,支持auc, acc, recall, precision'
}]

print("##### Generate XGB setting template")
print("%s" % json.dumps(common_template + xgb_model_template, ensure_ascii=False))
print("##### Generate XGB setting template done")

lr_model_template = [{
    'label': 'epochs',
    'type': 'int',
    'value': 3,
    'min_value': 1,
    'description': '数据迭代轮数'
},
    {
    'label': 'batch_size',
        'type': 'int',
        'value': 16,
        'min_value': 1,
        'description': '每次训练迭代中使用的数据量'
},
    {
    'label': 'feature_rate',
        'type': 'float',
        'value': 1.0,
        'description': ''
}
]

print("##### Generate LR setting template")
print("%s" % json.dumps(common_template + lr_model_template, ensure_ascii=False))
print("##### Generate LR setting template done")
