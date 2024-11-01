/*
 * Copyright 2017-2025  [webank-wedpr]
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */

package com.webank.wedpr.components.project.dao;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ProjectMapper {

    // insert the project information
    public int insertProjectInfo(@Param("projectDO") ProjectDO projectDO);
    // update the project information
    public int updateProjectInfo(
            @Param("owner") String owner, @Param("projectDO") ProjectDO projectDO);

    public int batchUpdateProjectInfo(@Param("projectDOList") List<ProjectDO> projectDOList);
    // delete projects
    public int deleteProjects(
            @Param("owner") String owner, @Param("projectList") List<String> projectList);
    // query the projects according to given condition
    public List<ProjectDO> queryProject(
            @Param("onlyMeta") Boolean onlyMeta, @Param("condition") ProjectDO condition);
    // query the project count according to given condition
    public long queryProjectCount(@Param("condition") ProjectDO condition);

    public Long queryJobCount(@Param("condition") JobDO condition);

    public Long queryFollowerJobCount(
            @Param("followerUser") String followerUser,
            @Param("followerAgency") String followerAgency,
            @Param("condition") JobDO condition);

    public int insertJobInfo(@Param("jobDO") JobDO jobDO);

    public int updateJobInfo(@Param("jobDO") JobDO jobDO);

    public int batchUpdateJobInfo(@Param("jobDOList") List<JobDO> jobDOList);

    public int batchInsertJobDatasetRelationInfo(
            @Param("jobID") String jobID, @Param("datasetIDs") List<String> datasetIDs);

    public List<JobDO> queryJobs(
            @Param("onlyMeta") Boolean onlyMeta,
            @Param("condition") JobDO condition,
            @Param("jobIDList") List<String> jobIDList);

    public List<JobDO> queryFollowerJobByCondition(
            @Param("onlyMeta") Boolean onlyMeta,
            @Param("followerUser") String followerUser,
            @Param("followerAgency") String followerAgency,
            @Param("condition") JobDO condition);

    public List<JobDO> queryJobsByDatasetID(@Param("datasetID") String datasetID);

    public List<JobDatasetDO> queryJobDatasetInfo(@Param("condition") JobDatasetDO condition);

    void batchUpdateJobDatasetInfo(@Param("jobDatasetDOList") List<JobDatasetDO> jobDatasetDOList);
}
