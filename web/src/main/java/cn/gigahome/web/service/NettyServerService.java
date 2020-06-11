package cn.gigahome.web.service;

import cn.gigahome.web.netty.examples.NettyServer;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class NettyServerService {
    @PostConstruct
    public void startNettyServer() {
        ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
            Thread newThread = new Thread(r);
            newThread.setDaemon(true);
            newThread.setName("nettyServer");
            return newThread;
        });
        executorService.submit(new NettyServer());
    }
}
