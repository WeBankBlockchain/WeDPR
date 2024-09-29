package com.webank.wedpr.components.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.webank.wedpr.components.admin.request.GetDatasetDateLineRequest;
import com.webank.wedpr.components.admin.request.GetWedprDatasetListRequest;
import com.webank.wedpr.components.admin.response.GetDatasetLineResponse;
import com.webank.wedpr.components.admin.response.GetDatasetStatisticsResponse;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;

/**
 * 数据集记录表 服务类
 *
 * @author caryliao
 * @since 2024-08-29
 */
public interface WedprDatasetService extends IService<Dataset> {

    ListDatasetResponse listDataset(GetWedprDatasetListRequest getWedprDatasetListRequest);

    GetDatasetStatisticsResponse getDatasetStatistics();

    GetDatasetLineResponse getDatasetDateLine(GetDatasetDateLineRequest getDatasetDateLineRequest);
}
