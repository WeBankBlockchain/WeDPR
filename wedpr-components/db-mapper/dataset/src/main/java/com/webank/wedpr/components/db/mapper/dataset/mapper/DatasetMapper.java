package com.webank.wedpr.components.db.mapper.dataset.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.webank.wedpr.components.db.mapper.dataset.dao.Dataset;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasetMapper extends BaseMapper<Dataset> {

    /**
     * query dataset by dataset_id
     *
     * @param datasetId
     * @param isTx
     * @return
     */
    Dataset getDatasetByDatasetId(
            @Param("datasetId") String datasetId, @Param("isTx") boolean isTx);

    /**
     * query dataset id for check if dataset exist
     *
     * @param datasetId
     * @return
     */
    String getDatasetId(@Param("datasetId") String datasetId);

    /**
     * query all visible dataset for the user
     *
     * @return
     */
    List<Dataset> queryVisibleDatasetsForUser(
            @Param("loginUser") String loginUser,
            @Param("loginAgency") String loginAgency,
            @Param("loginUserSubject") String loginUserSubject,
            @Param("loginUserGroupSubjectList") List<String> loginUserGroupSubjectList,
            @Param("ownerUser") String ownerUser,
            @Param("ownerAgency") String ownerAgency,
            @Param("datasetTitle") String datasetTitle,
            @Param("permissionType") Integer permissionType,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    /**
     * insert dataset
     *
     * @param dataset
     * @return
     */
    int insertDataset(Dataset dataset);

    /**
     * update dataset
     *
     * @param dataset
     * @return
     */
    int updateDataset(Dataset dataset);

    /**
     * delete dataset
     *
     * @param datasetId
     * @return
     */
    int deleteDataset(String datasetId);

    /**
     * update dataset status field
     *
     * @param datasetId
     * @param status
     * @param statusDesc
     * @return
     */
    int updateDatasetStatus(
            @Param("datasetId") String datasetId,
            @Param("status") int status,
            @Param("statusDesc") String statusDesc);

    /**
     * @param dataset
     * @return
     */
    int updateDatasetMetaInfo(Dataset dataset);

    int getUseCountByDataSourceType(@Param("dataSourceType") String dataSourceType);

    List<Dataset> getDatasetDateLine(
            @Param("agencyName") String agencyName,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    List<Dataset> datasetTypeStatistic();

    Dataset datasetAgencyStatistic(@Param("agencyName") String agencyName);

    Dataset datasetAgencyTypeStatistic(
            @Param("agencyName") String agencyName, @Param("dataSourceType") String dataSourceType);
}
