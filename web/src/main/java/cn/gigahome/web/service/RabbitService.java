package cn.gigahome.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class RabbitService {
    Logger logger = LoggerFactory.getLogger(RabbitService.class);
    private static MessageProperties messageProperties = new MessageProperties();

    static {
        messageProperties.setContentEncoding("UTF-8");
        messageProperties.setRedelivered(true);
    }

    @RabbitListener(ackMode = "MANUAL", queues = "APPLE")
    public void receive(Message message) {
        logger.info("TOPIC APPLE -> {}", new String(message.getBody()));
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void sendMessage(String topic, String messageContent) {
        try {
            Message message = new Message(messageContent.getBytes(StandardCharsets.UTF_8), messageProperties);
            rabbitTemplate.send(topic, message);
        } catch (Exception ex) {
            logger.error("发送消息失败：{}", ex.getMessage());
        }
    }
}
