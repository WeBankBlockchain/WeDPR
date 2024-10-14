package com.webank.wedpr.components.dataset.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.webank.wedpr.common.utils.ObjectMapperFactory;
import com.webank.wedpr.components.authorization.WeDPRAuthNotifier;
import com.webank.wedpr.components.authorization.core.AuthApplyType;
import com.webank.wedpr.components.authorization.dao.AuthorizationDO;
import com.webank.wedpr.components.dataset.service.DatasetAuthApi;
import com.webank.wedpr.components.db.mapper.dataset.dao.DatasetAuthContent;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetAuthNotifierConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatasetAuthNotifierConfig.class);

    @Autowired
    @Qualifier("datasetAuth")
    private DatasetAuthApi datasetAuth;

    @Bean
    void registerAuthNotifyHandler() {

        // Register Dataset Approval Callback
        WeDPRAuthNotifier.getAuthNotifier()
                .registerNotifyHandler(
                        AuthApplyType.DATASET.getType(),
                        args -> {
                            AuthorizationDO authData = args.getAuthData();

                            String id = authData.getId();
                            String status = authData.getStatus();
                            String applyContent = authData.getApplyContent();
                            String applicant = authData.getApplicant();
                            String applicantAgency = authData.getApplicantAgency();

                            UserInfo userInfo =
                                    UserInfo.builder()
                                            .user(applicant)
                                            .groupInfos(null)
                                            .agency(applicantAgency)
                                            .build();

                            logger.info(
                                    "receive the approval result for dataset permissions, id: {}, status: {}, applyChain: {}, applicantAgency: {}, applyContent: {}",
                                    id,
                                    status,
                                    applicant,
                                    applicantAgency,
                                    applyContent);

                            //                            if
                            // (!status.equalsIgnoreCase(AuthStatus.ApproveSuccess.getStatus())) {
                            //                                return;
                            //                            }

                            List<DatasetAuthContent> datasetAuthContentList = null;
                            try {
                                datasetAuthContentList =
                                        ObjectMapperFactory.getObjectMapper()
                                                .readValue(
                                                        applyContent,
                                                        new TypeReference<
                                                                List<DatasetAuthContent>>() {});
                            } catch (JsonProcessingException e) {
                                logger.error(
                                        "invalid json object, id: {}, applyContent: {}",
                                        id,
                                        applyContent);
                                return;
                            }

                            try {
                                datasetAuth.authorizeDatasetPermissionList(
                                        userInfo, datasetAuthContentList);
                            } catch (Exception e) {
                                logger.error(
                                        "dataset authorization failed, id: {}, applyContent: {}, e: ",
                                        id,
                                        applyContent,
                                        e);
                            }
                        });
    }
}
