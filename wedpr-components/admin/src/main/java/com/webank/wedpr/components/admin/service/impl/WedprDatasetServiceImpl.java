package com.webank.wedpr.components.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.entity.WedprAgency;
import com.webank.wedpr.components.admin.entity.WedprJobDatasetRelation;
import com.webank.wedpr.components.admin.request.GetDatasetDateLineRequest;
import com.webank.wedpr.components.admin.request.GetWedprDatasetListRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.admin.service.WedprDatasetService;
import com.webank.wedpr.components.admin.service.WedprJobDatasetRelationService;
import com.webank.wedpr.components.dataset.message.ListDatasetResponse;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import com.webank.wedpr.components.db.mapper.dataset.datasource.DataSourceType;
import com.webank.wedpr.components.db.mapper.dataset.mapper.DatasetMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 数据集记录表 服务实现类
 *
 * @author caryliao
 * @since 2024-08-29
 */
@Service
public class WedprDatasetServiceImpl extends ServiceImpl<DatasetMapper, Dataset>
        implements WedprDatasetService {

    @Value("${dashbord.decimalPlaces:0}")
    private Integer decimalPlaces;

    @Value("${dashbord.limitSize:5}")
    private Integer limitSize;

    @Autowired private WedprJobDatasetRelationService wedprJobDatasetRelationService;

    @Autowired private WedprAgencyService wedprAgencyService;

    @Autowired private DatasetMapper datasetMapper;

    @Override
    public ListDatasetResponse listDataset(GetWedprDatasetListRequest getWedprDatasetListRequest) {
        LambdaQueryWrapper<Dataset> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String ownerAgencyName = getWedprDatasetListRequest.getOwnerAgencyName();
        String datasetTitle = getWedprDatasetListRequest.getDatasetTitle();
        String startTimeStr = getWedprDatasetListRequest.getStartTime();
        String endTimeStr = getWedprDatasetListRequest.getEndTime();
        Integer pageNum = getWedprDatasetListRequest.getPageNum();
        Integer pageSize = getWedprDatasetListRequest.getPageSize();
        if (!StringUtils.isEmpty(ownerAgencyName)) {
            lambdaQueryWrapper.like(Dataset::getOwnerAgencyName, ownerAgencyName);
        }
        if (!StringUtils.isEmpty(datasetTitle)) {
            lambdaQueryWrapper.like(Dataset::getDatasetTitle, datasetTitle);
        }
        if (!StringUtils.isEmpty(startTimeStr)) {
            LocalDateTime startTime = Utils.getLocalDateTime(startTimeStr);
            lambdaQueryWrapper.ge(Dataset::getCreateAt, startTime);
        }
        if (!StringUtils.isEmpty(endTimeStr)) {
            LocalDateTime endTime = Utils.getLocalDateTime(endTimeStr);
            lambdaQueryWrapper.le(Dataset::getCreateAt, endTime);
        }
        lambdaQueryWrapper.orderByDesc(Dataset::getUpdateAt);
        Page<Dataset> datasetPage = new Page<>(pageNum, pageSize);
        Page<Dataset> page = page(datasetPage, lambdaQueryWrapper);
        ListDatasetResponse listDatasetResponse =
                ListDatasetResponse.builder()
                        .totalCount(page.getTotal())
                        .isLast(!page.hasNext())
                        .content(page.getRecords())
                        .build();
        return listDatasetResponse;
    }

    @Override
    public GetDatasetStatisticsResponse getDatasetStatistics() {
        // query dataset overview
        int totalCount = count();
        QueryWrapper<WedprJobDatasetRelation> jobDatasetRelationQueryWrapper1 =
                new QueryWrapper<>();
        jobDatasetRelationQueryWrapper1.select("DISTINCT dataset_id");
        int usedCount = wedprJobDatasetRelationService.count(jobDatasetRelationQueryWrapper1);
        String usedProportion = Utils.getPercentage(usedCount, totalCount, decimalPlaces);
        DatasetOverview datasetOverview = new DatasetOverview();
        datasetOverview.setTotalCount(totalCount);
        datasetOverview.setUsedCount(usedCount);
        datasetOverview.setUsedProportion(usedProportion);

        // query datasetTypeStatistic
        List<Dataset> datasetList1 = datasetMapper.datasetTypeStatistic();
        List<DatasetTypeStatistic> datasetTypeStatisticList = new ArrayList<>();
        DataSourceType[] dataSourceTypes = DataSourceType.values();
        for (DataSourceType dataSourceTypeItem : dataSourceTypes) {
            DatasetTypeStatistic datasetTypeStatistic = new DatasetTypeStatistic();
            String dataSourceType = dataSourceTypeItem.name();
            datasetTypeStatistic.setDatasetType(dataSourceType);
            datasetTypeStatistic.setCount(0);
            datasetTypeStatistic.setUsedProportion("0");
            for (Dataset dataset : datasetList1) {
                if (dataSourceType.equals(dataset.getDataSourceType())) {
                    Integer countByDataSourceType = dataset.getCount();
                    datasetTypeStatistic.setCount(countByDataSourceType);
                    int usedCountByDataSourceType =
                            datasetMapper.getUseCountByDataSourceType(dataSourceType);
                    datasetTypeStatistic.setUsedProportion(
                            Utils.getPercentage(
                                    usedCountByDataSourceType,
                                    countByDataSourceType,
                                    decimalPlaces));
                }
            }
            datasetTypeStatisticList.add(datasetTypeStatistic);
        }

        // query agencyDatasetTypeStatistic
        List<WedprAgency> wedprAgencyList = wedprAgencyService.list();
        ArrayList<AgencyDatasetTypeStatistic> agencyDatasetTypeStatisticList =
                new ArrayList<>(wedprAgencyList.size());
        for (WedprAgency wedprAgency : wedprAgencyList) {
            String agencyName = wedprAgency.getAgencyName();
            Dataset dataset1 = datasetMapper.datasetAgencyStatistic(agencyName);
            AgencyDatasetTypeStatistic agencyDatasetTypeStatistic =
                    new AgencyDatasetTypeStatistic();
            agencyDatasetTypeStatistic.setAgencyName(agencyName);
            agencyDatasetTypeStatistic.setTotalCount(0);
            if (dataset1 != null) {
                agencyDatasetTypeStatistic.setTotalCount(dataset1.getCount());
            }
            List<DatasetTypeStatistic> datasetTypeStatisticsList = new ArrayList<>();
            for (DataSourceType dataSourceTypeItem : dataSourceTypes) {
                String dataSourceType = dataSourceTypeItem.name();
                Dataset dataset2 =
                        datasetMapper.datasetAgencyTypeStatistic(agencyName, dataSourceType);
                DatasetTypeStatistic datasetTypeStatistic = new DatasetTypeStatistic();
                datasetTypeStatistic.setDatasetType(dataSourceType);
                datasetTypeStatistic.setCount(0);
                if (dataset2 != null) {
                    datasetTypeStatistic.setCount(dataset2.getCount());
                }
                datasetTypeStatisticsList.add(datasetTypeStatistic);
            }
            agencyDatasetTypeStatistic.setDatasetTypeStatistic(datasetTypeStatisticsList);
            agencyDatasetTypeStatisticList.add(agencyDatasetTypeStatistic);
        }
        Collections.sort(
                agencyDatasetTypeStatisticList,
                (o1, o2) -> o2.getTotalCount() - o1.getTotalCount());
        List<AgencyDatasetTypeStatistic> sortedAgencyDatasetTypeStatisticList =
                agencyDatasetTypeStatisticList
                        .stream()
                        .limit(limitSize)
                        .collect(Collectors.toList());
        GetDatasetStatisticsResponse response = new GetDatasetStatisticsResponse();
        response.setDatasetOverview(datasetOverview);
        response.setDatasetTypeStatistic(datasetTypeStatisticList);
        response.setAgencyDatasetTypeStatistic(sortedAgencyDatasetTypeStatisticList);
        return response;
    }

    @Override
    public GetDatasetLineResponse getDatasetDateLine(
            GetDatasetDateLineRequest getDatasetDateLineRequest) {
        String startTime = getDatasetDateLineRequest.getStartTime();
        String endTime = getDatasetDateLineRequest.getEndTime();

        List<WedprAgency> wedprAgencyList = wedprAgencyService.list();
        ArrayList<AgencyDatasetTypeStatistic> agencyDatasetTypeStatisticList =
                new ArrayList<>(wedprAgencyList.size());
        for (WedprAgency wedprAgency : wedprAgencyList) {
            String agencyName = wedprAgency.getAgencyName();
            Dataset dataset1 = datasetMapper.datasetAgencyStatistic(agencyName);
            AgencyDatasetTypeStatistic agencyDatasetTypeStatistic =
                    new AgencyDatasetTypeStatistic();
            agencyDatasetTypeStatistic.setAgencyName(agencyName);
            agencyDatasetTypeStatistic.setTotalCount(0);
            if (dataset1 != null) {
                agencyDatasetTypeStatistic.setTotalCount(dataset1.getCount());
            }
            agencyDatasetTypeStatisticList.add(agencyDatasetTypeStatistic);
        }
        Collections.sort(
                agencyDatasetTypeStatisticList,
                (o1, o2) -> o2.getTotalCount() - o1.getTotalCount());
        List<AgencyDatasetTypeStatistic> sortedAgencyDatasetTypeStatisticList =
                agencyDatasetTypeStatisticList
                        .stream()
                        .limit(limitSize)
                        .collect(Collectors.toList());
        List<AgencyDatasetStat> agencyDatasetStatList = new ArrayList<>();
        for (AgencyDatasetTypeStatistic agencyDatasetTypeStatistic :
                sortedAgencyDatasetTypeStatisticList) {
            String agencyName = agencyDatasetTypeStatistic.getAgencyName();
            List<Dataset> datasetList =
                    datasetMapper.getDatasetDateLine(agencyName, startTime, endTime);
            AgencyDatasetStat agencyDatasetStat = new AgencyDatasetStat();
            agencyDatasetStat.setAgencyName(agencyName);
            int size = datasetList.size();
            List<String> dateList = new ArrayList<>(size);
            List<Integer> countList = new ArrayList<>(size);
            for (Dataset dataset : datasetList) {
                dateList.add(dataset.getCreateAt());
                countList.add(dataset.getCount());
            }
            agencyDatasetStat.setDateList(dateList);
            agencyDatasetStat.setCountList(countList);
            agencyDatasetStatList.add(agencyDatasetStat);
        }
        GetDatasetLineResponse response = new GetDatasetLineResponse();
        response.setAgencyDatasetStat(agencyDatasetStatList);
        return response;
    }
}
