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

package com.webank.wedpr.components.meta.sys.config.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.ArrayList;
import java.util.List;

public class SysConfigGenerator {
    public static void main(String[] args) throws JsonProcessingException {
        AlgorithmTemplateInfo algorithmInfo = new AlgorithmTemplateInfo();
        List<AlgorithmTemplateInfo.AlgorithmTemplate> templateList = new ArrayList<>();
        templateList.add(new AlgorithmTemplateInfo.AlgorithmTemplate("PSI", "数据对齐", ""));
        templateList.add(
                new AlgorithmTemplateInfo.AlgorithmTemplate("XGB_TRAINING", "SecureLGBM训练", ""));
        templateList.add(
                new AlgorithmTemplateInfo.AlgorithmTemplate("XGB_PREDICTING", "SecureLGBM预测", ""));
        algorithmInfo.setTemplates(templateList);
        System.out.println("====== SysConfigGenerator, generate algorithmTemplates:");
        System.out.println(algorithmInfo.serialize());
    }
}
