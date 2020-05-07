package cn.gigahome.common.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;

@ConfigurationProperties(prefix = "application")
public class ApplicationVersion {
    private Logger logger = LoggerFactory.getLogger(ApplicationVersion.class);

    @PostConstruct
    public void init() {
        logger.info("create an application version");
    }

    private String version = "1.0.0";

    private String lastCommitTime = "2020-01-13 17:42";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getLastCommitTime() {
        return lastCommitTime;
    }

    public void setLastCommitTime(String lastCommitTime) {
        this.lastCommitTime = lastCommitTime;
    }
}
