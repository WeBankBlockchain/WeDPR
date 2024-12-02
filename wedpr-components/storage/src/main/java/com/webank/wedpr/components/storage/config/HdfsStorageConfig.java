package com.webank.wedpr.components.storage.config;

import com.webank.wedpr.common.utils.Common;
import java.io.File;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "wedpr.storage.hdfs")
@Configuration
@Data
public class HdfsStorageConfig {
    private String user = "";
    private String url;
    private String baseDir = "/user";
    // the service config path
    private String serviceConfigPath = System.getProperty("serviceConfigPath");

    private Boolean enableKrb5Auth = false;
    private String krb5ConfigPath = "krb5.conf";
    // the auth principal, e.g.: root@NODE.DC1.CONSUL
    private String krb5Principal = "";
    // the keytab path
    private String krb5KeytabPath = "hdfs-wedpr.keytab";

    public String getAbsPathInHdfs(String path) {
        return getBaseDir() + File.separator + path;
    }

    public void setEnableKrb5Auth(Boolean enableKrb5Auth) {
        if (enableKrb5Auth == null) {
            return;
        }
        this.enableKrb5Auth = enableKrb5Auth;
    }

    public String getServiceConfigPath() {
        return serviceConfigPath;
    }

    public String getKrb5ConfigPath() {
        if (krb5ConfigPath.startsWith("/") || serviceConfigPath == null) {
            return krb5ConfigPath;
        }
        return serviceConfigPath + File.separator + krb5ConfigPath;
    }

    public String getKrb5KeytabPath() {
        if (krb5KeytabPath.startsWith("/") || serviceConfigPath == null) {
            return krb5KeytabPath;
        }
        return serviceConfigPath + File.separator + krb5KeytabPath;
    }

    public void check() {
        // the user should non-empty
        Common.requireNonEmpty("wedpr.storage.hdfs.user", user);
        // the url should non-empty
        Common.requireNonEmpty("wedpr.storage.hdfs.url", url);
        if (!enableKrb5Auth) {
            return;
        }
        Common.requireNonEmpty("wedpr.storage.hdfs.krb5Principal", krb5Principal);
        Common.requireNonEmpty("wedpr.storage.hdfs.krb5KeytabPath", krb5KeytabPath);
    }

    @Override
    public String toString() {
        return "HdfsStorageConfig{"
                + "user='"
                + user
                + '\''
                + ", url='"
                + url
                + '\''
                + ", baseDir='"
                + baseDir
                + '\''
                + ", enableKrb5Auth="
                + enableKrb5Auth
                + ", krb5ConfigPath='"
                + getKrb5ConfigPath()
                + '\''
                + ", krb5Principal='"
                + krb5Principal
                + '\''
                + ", krb5KeytabPath='"
                + getKrb5KeytabPath()
                + '\''
                + '}';
    }
}
