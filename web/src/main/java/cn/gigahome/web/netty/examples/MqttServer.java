package cn.gigahome.web.netty.examples;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.*;

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
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new SimpleServerHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            //在这里加入处理读写的handler
                            ChannelPipeline pipeline = ch.pipeline();
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
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            MqttMessage message = (MqttMessage) msg;
            System.out.println(message.payload().toString());
            MqttFixedHeader connackFixedHeader =
                    new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
            MqttConnAckVariableHeader mqttConnAckVariableHeader =
                    new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
            MqttConnAckMessage connack = new MqttConnAckMessage(connackFixedHeader, mqttConnAckVariableHeader);
            ctx.writeAndFlush(connack);
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
