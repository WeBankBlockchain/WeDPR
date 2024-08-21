package com.webank.wedpr.components.mybatis.tool;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.po.LikeTable;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import java.util.ArrayList;
import java.util.List;

/** Created by caryliao on 2024/7/15 20:26 */
public class GenerateCodeMain {
    public static void main(String[] args) {
        String moduleName = "user";
        String tableName = "wedpr*";
        String prefixName = "";

        // 代码生成器
        AutoGenerator mpg = new AutoGenerator();

        // 数据源配置
        com.baomidou.mybatisplus.generator.config.DataSourceConfig dsc = new DataSourceConfig();
        dsc.setUrl(
                "jdbc:mysql://127.0.0.1:3306/wedpr3?characterEncoding=utf8&useSSL=false&serverTimezone=UTC&");
        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
        dsc.setUsername("root");
        dsc.setPassword("Wedpr2023");
        mpg.setDataSource(dsc);

        // 全局配置
        GlobalConfig gc = new GlobalConfig();
        // 获得当前项目的路径
        String projectPath = System.getProperty("user.dir") + "/wedpr-components/" + moduleName;
        // 设置生成路径
        gc.setOutputDir(projectPath + "/src/main/java");
        // 作者
        gc.setAuthor("caryliao");
        // 代码生成是不是要打开所在文件夹
        gc.setOpen(false);
        // 生成Swagger2注解
        gc.setSwagger2(true);
        // 会在mapper.xml 生成一个基础的<ResultMap> 映射所有的字段
        gc.setBaseResultMap(true);
        // 同文件生成覆盖
        gc.setFileOverride(true);
        // gc.setDateType(DateType.ONLY_DATE)
        // 实体名：直接用表名  %s=表名
        gc.setEntityName("%s");
        // mapper接口名
        gc.setMapperName("%sMapper");
        // mapper.xml 文件名
        gc.setXmlName("%sMapper");
        // 业务逻辑类接口名
        gc.setServiceName("%sService");
        // 业务逻辑类实现类名
        gc.setServiceImplName("%sImplService");
        // 将全局配置设置到AutoGenerator
        mpg.setGlobalConfig(gc);

        // 包配置
        PackageConfig pc = new PackageConfig();
        // 模块名
        pc.setModuleName(moduleName);
        // 包名
        pc.setParent("com.webank.wedpr.components");
        // 完整的报名：com.webank.wedpr.components.user
        mpg.setPackageInfo(pc);

        // 自定义配置
        InjectionConfig cfg =
                new InjectionConfig() {
                    @Override
                    public void initMap() {
                        // to do nothing
                    }
                };

        // 如果模板引擎是 velocity
        String templatePath = "/templates/mapper.xml.vm";
        // 自定义输出配置
        List<FileOutConfig> focList = new ArrayList<>();
        // 自定义配置会被优先输出
        focList.add(
                new FileOutConfig(templatePath) {
                    @Override
                    public String outputFile(TableInfo tableInfo) {
                        // 自定义输出文件名 ， 如果你 Entity 设置了前后缀、此处注意 xml 的名称会跟着发生变化！！
                        return projectPath
                                + "/src/main/resources/mapper/"
                                + "/"
                                + tableInfo.getEntityName()
                                + "Mapper"
                                + StringPool.DOT_XML;
                    }
                });

        cfg.setFileOutConfigList(focList);
        mpg.setCfg(cfg);

        // 配置模板
        TemplateConfig templateConfig = new TemplateConfig();

        // 把已有的xml生成置空
        templateConfig.setXml(null);
        mpg.setTemplate(templateConfig);

        // 策略配置
        StrategyConfig strategy = new StrategyConfig();
        // 表名的生成策略：下划线转驼峰 pms_product -- PmsProduct
        strategy.setNaming(NamingStrategy.underline_to_camel);
        // 列名的生成策略：下划线转驼峰 last_name -- lastName
        strategy.setColumnNaming(NamingStrategy.underline_to_camel);
        // strategy.setSuperEntityClass("你自己的父类实体,没有就不用设置!");
        // strategy.setEntityLombokModel(true);
        // 在controller类上是否生成@RestController
        strategy.setRestControllerStyle(true);
        // 公共父类
        // strategy.setSuperControllerClass("你自己的父类控制器,没有就不用设置!");

        if (tableName.indexOf('*') > 0) {
            // 按前缀生成表
            strategy.setLikeTable(new LikeTable(tableName.replace('*', '_')));
        } else {
            // 要生成的表名 多个用逗号分隔
            strategy.setInclude(tableName);
        }
        // 设置表替换前缀
        strategy.setTablePrefix(prefixName);
        // 驼峰转连字符 比如 pms_product --> controller @RequestMapping("/pms/pmsProduct")
        // strategy.setControllerMappingHyphenStyle(true);
        mpg.setStrategy(strategy);

        // 进行生成
        mpg.execute();
    }
}
