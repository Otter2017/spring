package cn.gigahome.common.config;

import cn.gigahome.common.bean.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
public class CommonAutoConfig {

    @Bean
    @ConditionalOnWebApplication
    Apple createApple() {
        return new Apple();
    }

    @Bean
    @ConditionalOnMissingBean(ApplicationVersion.class)
    @ConditionalOnBean(Apple.class)
    ApplicationVersion applicationVersion() {
        return new ApplicationVersion();
    }

    @Bean
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).isEmpty('${animal.type:}')")
    Animal createCat() {
        return new Cat();
    }

    @Bean
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${animal.type:}')")
    Animal createDog() {
        return new Dog();
    }
}
