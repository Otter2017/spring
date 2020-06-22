package cn.gigahome.web.netty.examples;

import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageProcessor implements Runnable {
    Logger logger = LoggerFactory.getLogger(MessageProcessor.class);

    private ConcurrentLinkedQueue<MqttMessage> messageQueue;

    public MessageProcessor() {
        messageQueue = new ConcurrentLinkedQueue<>();
    }

    public void addMessage(MqttMessage mqttMessage) {
        this.messageQueue.add(mqttMessage);
    }

    @Override
    public void run() {
        for (; ; ) {
            MqttMessage message = messageQueue.poll();
            if (message != null) {
                if (message.fixedHeader().messageType() == MqttMessageType.PUBLISH) {
                    MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) message;
                    String topic = mqttPublishMessage.variableHeader().topicName();
                    String content = mqttPublishMessage.payload().toString(StandardCharsets.UTF_8);
                    logger.info("收到消息: {} @ {}", content, topic);
                }
            }
        }
    }
}
