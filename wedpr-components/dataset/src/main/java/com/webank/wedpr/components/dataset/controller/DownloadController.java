package com.webank.wedpr.components.dataset.controller;

import com.webank.wedpr.components.dataset.config.DatasetConfig;
import com.webank.wedpr.components.dataset.message.DownloadFileShardRequest;
import com.webank.wedpr.components.dataset.message.GetFileShardsInfoResponse;
import com.webank.wedpr.components.dataset.service.DownloadServiceApi;
import com.webank.wedpr.components.dataset.utils.UserTokenUtils;
import com.webank.wedpr.components.db.mapper.dataset.common.DatasetConstant;
import com.webank.wedpr.components.db.mapper.dataset.dao.UserInfo;
import com.webank.wedpr.core.utils.Common;
import com.webank.wedpr.core.utils.Constant;
import com.webank.wedpr.core.utils.WeDPRResponse;
import java.io.File;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(DatasetConstant.WEDPR_DATASET_API_PREFIX)
@Data
public class DownloadController {

    private static final Logger logger = LoggerFactory.getLogger(DownloadController.class);

    @Autowired private DatasetConfig datasetConfig;

    @Qualifier("downloadService")
    @Autowired
    private DownloadServiceApi downloadService;

    @GetMapping("getFileShardsInfo")
    public WeDPRResponse getFileShardsInfo(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "filePath") String filePath) {

        long startTimeMillis = System.currentTimeMillis();
        logger.info("get file shards info begin, filePath:{}", filePath);

        WeDPRResponse weDPRResponse =
                new WeDPRResponse(Constant.WEDPR_SUCCESS, Constant.WEDPR_SUCCESS_MSG);

        try {
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            int shardCount = downloadService.getFileShardsInfo(userInfo, filePath);

            GetFileShardsInfoResponse getFileShardsInfoResponse = new GetFileShardsInfoResponse();
            getFileShardsInfoResponse.setShardCount(shardCount);

            weDPRResponse.setData(getFileShardsInfoResponse);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "get file shards info success, filePath: {}, shardCount: {}, cost(ms): {}",
                    filePath,
                    shardCount,
                    (endTimeMillis - startTimeMillis));

        } catch (Exception e) {
            weDPRResponse.setCode(Constant.WEDPR_FAILED);
            weDPRResponse.setMsg(e.getMessage());

            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "get file shards info Exception, filePath: {}, cost(ms): {}, e: ",
                    filePath,
                    (endTimeMillis - startTimeMillis),
                    e);
        }

        return weDPRResponse;
    }

    @SneakyThrows
    @PostMapping("downloadFileShardData")
    public ResponseEntity<Void> downloadFileShardData(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            @RequestBody DownloadFileShardRequest downloadFileShardRequest) {

        long startTimeMillis = System.currentTimeMillis();
        logger.info("download file shards data begin, request:{}", downloadFileShardRequest);

        try {
            UserInfo userInfo = UserTokenUtils.getUserInfo(datasetConfig, httpServletRequest);

            String filePath = downloadFileShardRequest.getFilePath();
            Common.requireNonNull("filePath", filePath);
            Integer shardIndex = downloadFileShardRequest.getShardIndex();
            Common.requireNonNull("shardIndex", shardIndex);
            Integer shardCount = downloadFileShardRequest.getShardCount();
            Common.requireNonNull("shardCount", shardCount);

            ServletOutputStream outputStream = httpServletResponse.getOutputStream();

            String fileName = new File(filePath).getName();

            httpServletResponse.setContentType("application/octet-stream");
            httpServletResponse.setHeader("content-type", "application/octet-stream");
            httpServletResponse.setHeader("Content-Disposition", "attachment;fileName=" + fileName);

            downloadService.downloadFileShardData(
                    userInfo, filePath, shardCount, shardIndex, outputStream);

            long endTimeMillis = System.currentTimeMillis();
            logger.info(
                    "download file shards data success, request:{}, cost(ms): {}",
                    downloadFileShardRequest,
                    (endTimeMillis - startTimeMillis));

            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (Exception e) {
            long endTimeMillis = System.currentTimeMillis();
            logger.error(
                    "download file shards data Exception, cost(ms): {}, e: ",
                    (endTimeMillis - startTimeMillis),
                    e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
