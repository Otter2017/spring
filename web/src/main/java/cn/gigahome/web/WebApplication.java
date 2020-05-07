package cn.gigahome.web;

import cn.gigahome.common.bean.Animal;
import cn.gigahome.common.bean.ApplicationVersion;
import cn.gigahome.web.entity.RepeatableScheduleTask;
import cn.gigahome.web.entity.TaskExecutor;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@RestController
public class WebApplication {
    Logger logger = LoggerFactory.getLogger(WebApplication.class);

    public static void main(final String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    private TaskExecutor executor;

    private ApplicationVersion applicationVersion;

    private Animal animal;

    @Bean
    public WebMvcConfig webMvcConfig() {
        return new WebMvcConfig();
    }

    @Value("${animal.type:}")
    private String animalType;

    @Autowired
    public void setExecutor(TaskExecutor executor) {
        this.executor = executor;
    }

    @Autowired
    public void setApplicationVersion(ApplicationVersion applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Autowired
    public void setAnimal(Animal animal) {
        this.animal = animal;
    }

    @Bean
    TaskExecutor init() {
        return new TaskExecutor();
    }

    @GetMapping("/version")
    public String getVersion() {
        return applicationVersion.getVersion();
    }

    @GetMapping("/log")
    public void log(@RequestParam(name = "delay") int delay, @RequestParam(name = "times") int times) {
        executor.execute(new RepeatableScheduleTask(delay, TimeUnit.SECONDS, times));
    }

    @GetMapping("/animal")
    public String getAnimal(HttpServletRequest request, HttpServletResponse response) {
        String url = request.getRequestURI();
        String ip = request.getRemoteHost();
        String method = request.getMethod();

        logger.info("{} {} from {}", method, url, ip);
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            Enumeration<String> headerValues = request.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                logger.info("header {} > {}", headerName, headerValues.nextElement());
            }
        }

        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String parameterName = parameterNames.nextElement();
            String[] parameterValues = request.getParameterValues(parameterName);
            for (String parameterValue : parameterValues) {
                logger.info("parameter {} = {}", parameterName, parameterValue);
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                logger.info("cookie {} > {}", cookie.getName(), cookie.getValue());
            }
        }
        if (!containCookie("token", cookies)) {
            Cookie cookie = new Cookie("token", UUID.randomUUID().toString());
            response.addCookie(cookie);
        }
        return animal.getName() + " > " + animalType;
    }

    private boolean containCookie(String cookieName, Cookie[] cookies) {
        if (cookies == null) {
            return false;
        }
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return true;
            }
        }
        return false;
    }

    @GetMapping("/now")
    public long currentTimestamp() {
        return System.currentTimeMillis();
    }

    @PostMapping("/live/record")
    public boolean recordLive(@RequestBody JSONObject data) {
        logger.info(JSON.toJSONString(data));
        return true;
    }
}
