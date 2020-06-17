package cn.gigahome.web.service;

import cn.gigahome.web.netty.examples.MqttServer;
import cn.gigahome.web.netty.examples.NettyServer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NettyServerService {
    @PostConstruct
    public void startNettyServer() {
        ExecutorService executorService = Executors.newFixedThreadPool(2, r -> {
            Thread newThread = new Thread(r);
            newThread.setDaemon(true);
            newThread.setName("nettyServer");
            return newThread;
        });
        executorService.submit(new MqttServer(1883));
        executorService.submit(new NettyServer(1884));
    }
}
