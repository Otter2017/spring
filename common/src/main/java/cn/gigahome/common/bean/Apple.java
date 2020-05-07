package cn.gigahome.common.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Apple {
    private Logger logger = LoggerFactory.getLogger(Apple.class);

    public Apple() {
        logger.info("create an apple");
    }
}
