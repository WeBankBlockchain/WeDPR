package com.webank.wedpr.components.db.mapper.dataset.mapper;

import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetPermission;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DatasetPermissionMapper {
    /**
     * @param datasetId
     * @param user
     * @param agency
     * @return
     */
    List<DatasetPermission> queryPermissionListForDataset(
            @Param("datasetId") String datasetId,
            @Param("user") String user,
            @Param("agency") String agency,
            @Param("isTx") boolean isTx);

    /**
     * query dataset permissions by dataset_id
     *
     * @param datasetId
     * @return
     */
    int deleteDatasetPermissionListByDatasetId(String datasetId);

    /**
     * batch insert dataset permission list
     *
     * @param datasetPermissionList
     * @return
     */
    int insertDatasetPermissionList(List<DatasetPermission> datasetPermissionList);

    /**
     * @param datasetPermission
     * @return
     */
    int deleteDatasetPermission(DatasetPermission datasetPermission);
}
