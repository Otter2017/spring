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

import java.nio.charset.StandardCharsets;

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
                            pipeline.addLast(sslCtx.newHandler(ch.alloc()));
                            /* 客户端需要添加以下代码,才能保证ssl正确处理

                            final SslContext sslCtx = SslContextBuilder.forClient()
                                    .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(),HOST, PORT));

                             */
                            pipeline.addLast(new MqttDecoder());
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

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            MqttMessage mqttMessage = (MqttMessage) msg;
            logger.info("Received MQTT message: " + mqttMessage);
            switch (mqttMessage.fixedHeader().messageType()) {
                case CONNECT:
                    logger.info("client connect");
                    MqttConnectMessage connectMessage = (MqttConnectMessage) mqttMessage;
                    String clientID = connectMessage.payload().clientIdentifier();
                    String userName = connectMessage.variableHeader().hasUserName() ? connectMessage.payload().userName() : null;
                    String password = connectMessage.variableHeader().hasPassword() ? connectMessage.payload().password() : null;
                    logger.info("connect from {}@{} - {}", userName, clientID, password);
                    ctx.writeAndFlush(generateConnAckMessage());
                    break;
                case PINGREQ:
                    logger.info("client ping");
                    MqttFixedHeader pingreqFixedHeader = new MqttFixedHeader(MqttMessageType.PINGRESP, false,
                            MqttQoS.AT_MOST_ONCE, false, 0);
                    MqttMessage pingResp = new MqttMessage(pingreqFixedHeader);
                    ctx.writeAndFlush(pingResp);
                    break;
                case PUBLISH:
                    logger.info("client publish");
                    MqttPublishMessage publishMessage = (MqttPublishMessage) mqttMessage;
                    int packId = publishMessage.variableHeader().packetId();
                    String topicName = publishMessage.variableHeader().topicName();
                    String content = publishMessage.payload().toString(StandardCharsets.UTF_8);
                    logger.info("message -> {}@{} - {}", packId, topicName, content);
                    MqttFixedHeader pubAckFixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false,
                            MqttQoS.AT_MOST_ONCE, false, 0);
                    MqttMessageIdVariableHeader idVariableHeader = MqttMessageIdVariableHeader.from(packId);
                    MqttMessage pubAck = new MqttMessage(pubAckFixedHeader, idVariableHeader);
                    ctx.writeAndFlush(pubAck);
                    break;
                case DISCONNECT:
                    logger.info("client disconnect");
                    ctx.close();
                    break;
                default:
                    logger.info("Unexpected message type: " + mqttMessage.fixedHeader().messageType());
                    ReferenceCountUtil.release(msg);
                    ctx.close();
            }
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
