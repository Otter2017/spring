package cn.gigahome.web.netty.examples;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttServer implements Runnable {

    private int port;

    public MqttServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            runNettyServer();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runNettyServer() throws Exception {
        boolean epollAvaiblable = Epoll.isAvailable();
        EventLoopGroup bossGroup = epollAvaiblable ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = epollAvaiblable ? new EpollEventLoopGroup() : new NioEventLoopGroup();
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            //todo 从文件读取key
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey())
                    .build();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(epollAvaiblable ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .handler(new SimpleServerHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //在这里加入处理读写的handler
                            ChannelPipeline pipeline = ch.pipeline();
//                            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
                            /* 客户端需要添加以下代码,才能保证ssl正确处理

                            final SslContext sslCtx = SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(),HOST, PORT));

                             */
                            // 默认的消息最大长度为8K,超过会DecoderResult会有相应的错误信息，且不会转成对应的消息实例
                            pipeline.addLast(new MqttDecoder(1024 * 16));
                            pipeline.addLast(MqttEncoder.INSTANCE);
                            pipeline.addLast(new MqttHandler());
                        }
                    });
            ChannelFuture f = b.bind(this.port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static class MqttHandler extends ChannelInboundHandlerAdapter {
        private Logger logger = LoggerFactory.getLogger(MqttServer.class);

        private ExecutorService executorService;

        private MessageProcessor messageProcessor;

        public MqttHandler() {
            executorService = Executors.newSingleThreadExecutor();
            messageProcessor = new MessageProcessor();
            executorService.submit(messageProcessor);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            MqttMessage mqttMessage = (MqttMessage) msg;
            logger.info("Received MQTT message from [{}], message = {} ", ctx.channel().remoteAddress(), mqttMessage);
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNECT: {
                    processConnectMessage(ctx, mqttMessage);
                    break;
                }
                case PINGREQ: {
                    processPingMessage(ctx);
                    break;
                }
                case PUBLISH:
                    processPublishMessage(ctx, mqttMessage);
                    break;
                case DISCONNECT:
                    logger.info("client disconnect");
                    ctx.close();
                    break;
                case SUBSCRIBE: {
                    processSubMessage(ctx, mqttMessage);
                    break;
                }
                case UNSUBSCRIBE: {
                    processUnSubMessage(ctx, mqttMessage);
                    break;
                }
                default:
                    logger.info("Unexpected message type: " + mqttMessage.fixedHeader().messageType());
                    ReferenceCountUtil.release(msg);
                    ctx.close();
            }
        }

        private void processConnectMessage(ChannelHandlerContext ctx, MqttMessage message) {
            logger.info("client connect");
            MqttConnectMessage connectMessage = (MqttConnectMessage) message;
            String clientID = connectMessage.payload().clientIdentifier();
            String userName = connectMessage.variableHeader().hasUserName() ? connectMessage.payload().userName() : null;
            String password = connectMessage.variableHeader().hasPassword() ? connectMessage.payload().password() : null;
            //todo 鉴权，记录登陆信息 clientid/userName
            logger.info("connect from {}@{} - {}", userName, clientID, password);
            ctx.writeAndFlush(generateConnAckMessage());
        }

        private void processPingMessage(ChannelHandlerContext ctx) {
            logger.info("client ping");
            MqttFixedHeader pingreqFixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
                    MqttQoS.AT_MOST_ONCE, false, 0);
            MqttMessage pingResp = new MqttMessage(pingreqFixedHeader);
            ctx.writeAndFlush(pingResp);
        }

        private void processPublishMessage(ChannelHandlerContext ctx, MqttMessage message) {
            logger.info("client publish");
            int qos = message.fixedHeader().qosLevel().value();
            //todo 判断是同步处理还是异步处理
            messageProcessor.addMessage(message);
            if (qos > 0) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) message;
                int packId = publishMessage.variableHeader().packetId();
                MqttFixedHeader pubAckFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false,
                        MqttQoS.AT_MOST_ONCE, false, 0);
                MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(packId);
                MqttMessage pubAck = new MqttMessage(pubAckFixedHeader, idVariableHeader);
                ctx.writeAndFlush(pubAck);
            }
        }

        private void processSubMessage(ChannelHandlerContext ctx, MqttMessage message) {
            MqttSubscribeMessage subscribeMessage = (MqttSubscribeMessage) message;
            int messageId = subscribeMessage.variableHeader().messageId();
            int topicCount = subscribeMessage.payload().topicSubscriptions().size();
            MqttFixedHeader subAckFixedHeader = new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
            List<Integer> qos = new ArrayList<>(topicCount);
            for (int i = 0; i < topicCount; i++) {
                qos.add(1);
            }
            MqttSubAckPayload subAckPayload = new MqttSubAckPayload(qos);
            MqttSubAckMessage subAckMessage = new MqttSubAckMessage(subAckFixedHeader, idVariableHeader, subAckPayload);
            ctx.writeAndFlush(subAckMessage);
        }

        private void processUnSubMessage(ChannelHandlerContext ctx, MqttMessage message) {
            MqttUnsubscribeMessage mqttUnsubscribeMessage = (MqttUnsubscribeMessage) message;
            int messageId = mqttUnsubscribeMessage.variableHeader().messageId();
            MqttFixedHeader unsubackFixedHeader =
                    new MqttFixedHeader(MqttMessageType.UNSUBACK, false, MqttQoS.AT_LEAST_ONCE, false, 0);
            MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(messageId);
            MqttUnsubAckMessage mqttUnsubAckMessage = new MqttUnsubAckMessage(unsubackFixedHeader, idVariableHeader);
            ctx.writeAndFlush(mqttUnsubAckMessage);
        }

        private MqttConnAckMessage generateConnAckMessage() {
            MqttFixedHeader connackFixedHeader =
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttConnAckVariableHeader mqttConnAckVariableHeader =
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
            return new MqttConnAckMessage(connackFixedHeader, mqttConnAckVariableHeader);
        }
    }

    private static class SimpleServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server channel Active");
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server channel Registered");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("server channel Added");
        }
    }
}
