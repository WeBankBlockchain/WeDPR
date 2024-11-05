package com.webank.wedpr.components.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.webank.wedpr.common.protocol.JobStatus;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.admin.common.Utils;
import com.webank.wedpr.components.admin.entity.WedprAgency;
import com.webank.wedpr.components.admin.entity.WedprJobTable;
import com.webank.wedpr.components.admin.mapper.WedprJobTableMapper;
import com.webank.wedpr.components.admin.request.GetJobDateLineRequest;
import com.webank.wedpr.components.admin.request.GetWedprJobListRequest;
import com.webank.wedpr.components.admin.response.*;
import com.webank.wedpr.components.admin.service.WedprAgencyService;
import com.webank.wedpr.components.admin.service.WedprJobTableService;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigDO;
import com.webank.wedpr.components.meta.sys.config.dao.SysConfigMapper;
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
 * 服务实现类
 *
 * @author caryliao
 * @since 2024-09-04
 */
@Service
public class WedprJobTableServiceImpl extends ServiceImpl<WedprJobTableMapper, WedprJobTable>
        implements WedprJobTableService {

    @Value("${dashbord.decimalPlaces:0}")
    private Integer decimalPlaces;

    @Value("${dashbord.limitSize:5}")
    private Integer limitSize;

    @Autowired private WedprJobTableMapper wedprJobTableMapper;

    @Autowired private WedprAgencyService wedprAgencyService;

    @Autowired private SysConfigMapper sysConfigMapper;

    @Override
    public ListJobResponse listJob(GetWedprJobListRequest request) {
        LambdaQueryWrapper<WedprJobTable> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        String projectId = request.getProjectId();
        String ownerAgency = request.getOwnerAgency();
        String jobType = request.getJobType();
        String status = request.getStatus();
        String startTimeStr = request.getStartTime();
        String endTimeStr = request.getEndTime();
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        lambdaQueryWrapper.eq(WedprJobTable::getProjectId, projectId);
        if (!StringUtils.isEmpty(ownerAgency)) {
            lambdaQueryWrapper.eq(WedprJobTable::getOwnerAgency, ownerAgency);
        }
        if (!StringUtils.isEmpty(jobType)) {
            lambdaQueryWrapper.eq(WedprJobTable::getJobType, jobType);
        }
        if (!StringUtils.isEmpty(status)) {
            lambdaQueryWrapper.eq(WedprJobTable::getStatus, status);
        }
        if (!StringUtils.isEmpty(startTimeStr)) {
            LocalDateTime startTime = Utils.getLocalDateTime(startTimeStr);
            lambdaQueryWrapper.ge(WedprJobTable::getCreateTime, startTime);
        }
        if (!StringUtils.isEmpty(endTimeStr)) {
            LocalDateTime endTime = Utils.getLocalDateTime(endTimeStr);
            lambdaQueryWrapper.le(WedprJobTable::getCreateTime, endTime);
        }
        lambdaQueryWrapper.orderByDesc(WedprJobTable::getLastUpdateTime);
        Page<WedprJobTable> projectTablePage = new Page<>(pageNum, pageSize);
        Page<WedprJobTable> page = page(projectTablePage, lambdaQueryWrapper);
        ListJobResponse listProjectResponse = new ListJobResponse();
        listProjectResponse.setTotal(page.getTotal());
        listProjectResponse.setJobList(page.getRecords());
        return listProjectResponse;
    }

    @Override
    public GetJobStatisticsResponse getJobStatistics() throws JsonProcessingException {
        // query job overview
        int totalCount = count();
        LambdaQueryWrapper<WedprJobTable> wedprJobTableQueryWrapper = new LambdaQueryWrapper<>();
        wedprJobTableQueryWrapper.eq(WedprJobTable::getStatus, JobStatus.RunSuccess.getStatus());
        int successCount = count(wedprJobTableQueryWrapper);
        String successProportion = Utils.getPercentage(successCount, totalCount, decimalPlaces);
        JobOverview jobOverview = new JobOverview();
        jobOverview.setTotalCount(totalCount);
        jobOverview.setSuccessCount(successCount);
        jobOverview.setSuccessProportion(successProportion);

        // query jobTypeStatistic
        List<WedprJobTable> jobTableList1 = wedprJobTableMapper.jobTypeStatistic();
        List<JobTypeStatistic> jobTypeStatisticList = new ArrayList<>();
        List<JobTypeConfig> jobTypeConfigList = getJobTypeConfigList();
        for (JobTypeConfig jobTypeConfig : jobTypeConfigList) {
            JobTypeStatistic jobTypeStatistic = new JobTypeStatistic();
            String jobTypeName = jobTypeConfig.getName();
            jobTypeStatistic.setJobType(jobTypeName);
            jobTypeStatistic.setJobTypeTitle(jobTypeConfig.getTitle());
            jobTypeStatistic.setCount(0);
            for (WedprJobTable wedprJobTable : jobTableList1) {
                if (jobTypeName.equals(wedprJobTable.getJobType())) {
                    jobTypeStatistic.setCount(wedprJobTable.getCount());
                }
            }
            jobTypeStatisticList.add(jobTypeStatistic);
        }

        // query agencyJobTypeStatistic
        List<WedprAgency> wedprAgencyList = wedprAgencyService.list();
        ArrayList<AgencyJobTypeStatistic> agencyJobTypeStatisticList =
                new ArrayList<>(wedprAgencyList.size());
        for (WedprAgency wedprAgency : wedprAgencyList) {
            String agencyName = wedprAgency.getAgencyName();
            WedprJobTable jobTable1 = wedprJobTableMapper.jobAgencyStatistic(agencyName);
            AgencyJobTypeStatistic agencyJobTypeStatistic = new AgencyJobTypeStatistic();
            agencyJobTypeStatistic.setAgencyName(agencyName);
            agencyJobTypeStatistic.setTotalCount(0);
            if (jobTable1 != null) {
                agencyJobTypeStatistic.setTotalCount(jobTable1.getCount());
            }
            List<JobTypeStatistic> jobTypeStatisticsList =
                    new ArrayList<>(jobTypeConfigList.size());
            for (JobTypeConfig jobTypeConfig : jobTypeConfigList) {
                String jobTypeName = jobTypeConfig.getName();
                WedprJobTable jobTable2 =
                        wedprJobTableMapper.jobAgencyTypeStatistic(agencyName, jobTypeName);
                JobTypeStatistic jobTypeStatistic = new JobTypeStatistic();
                jobTypeStatistic.setJobType(jobTypeName);
                jobTypeStatistic.setJobTypeTitle(jobTypeConfig.getTitle());
                jobTypeStatistic.setCount(0);
                if (jobTable2 != null) {
                    jobTypeStatistic.setCount(jobTable2.getCount());
                }
                jobTypeStatisticsList.add(jobTypeStatistic);
            }
            agencyJobTypeStatistic.setJobTypeStatistic(jobTypeStatisticsList);
            agencyJobTypeStatisticList.add(agencyJobTypeStatistic);
        }
        Collections.sort(
                agencyJobTypeStatisticList, (o1, o2) -> o2.getTotalCount() - o1.getTotalCount());
        List<AgencyJobTypeStatistic> sortedAgencyJobTypeStatisticList =
                agencyJobTypeStatisticList.stream().limit(limitSize).collect(Collectors.toList());
        GetJobStatisticsResponse response = new GetJobStatisticsResponse();
        response.setJobOverview(jobOverview);
        response.setJobTypeStatistic(jobTypeStatisticList);
        response.setAgencyJobTypeStatistic(sortedAgencyJobTypeStatisticList);
        return response;
    }

    private List<JobTypeConfig> getJobTypeConfigList() throws JsonProcessingException {
        SysConfigDO sysConfigDO = sysConfigMapper.queryConfig("wedpr_algorithm_templates");
        String configValue = sysConfigDO.getConfigValue();
        WedprJobTypeConfigWrapper wedprJobTypeConfigWrapper =
                ObjectMapperFactory.getObjectMapper()
                        .readValue(configValue, WedprJobTypeConfigWrapper.class);
        List<JobTypeConfig> jobTypeConfigList = wedprJobTypeConfigWrapper.getTemplates();
        return jobTypeConfigList;
    }

    @Override
    public GetJobLineResponse getJobDateLine(GetJobDateLineRequest getJobDateLineRequest)
            throws JsonProcessingException {
        String startTime = getJobDateLineRequest.getStartTime();
        String endTime = getJobDateLineRequest.getEndTime();
        List<JobTypeConfig> jobTypeConfigList = getJobTypeConfigList();
        List<JobTypeStat> jobTypeStatList = new ArrayList<>();
        for (JobTypeConfig jobTypeConfig : jobTypeConfigList) {
            String jobTypeName = jobTypeConfig.getName();
            List<WedprJobTable> jobList =
                    wedprJobTableMapper.getJobDateLine(jobTypeName, startTime, endTime);
            JobTypeStat jobTypeStat = new JobTypeStat();
            jobTypeStat.setJobType(jobTypeName);
            jobTypeStat.setJobTypeTile(jobTypeConfig.getTitle());
            int size = jobList.size();
            List<String> dateList = new ArrayList<>(size);
            List<Integer> countList = new ArrayList<>(size);
            for (WedprJobTable wedprJobTable : jobList) {
                dateList.add(wedprJobTable.getCreateTime());
                countList.add(wedprJobTable.getCount());
            }
            jobTypeStat.setDateList(dateList);
            jobTypeStat.setCountList(countList);
            jobTypeStatList.add(jobTypeStat);
        }
        GetJobLineResponse response = new GetJobLineResponse();
        response.setJobTypeStat(jobTypeStatList);
        return response;
    }
}
