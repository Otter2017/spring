package cn.gigahome.web.netty.examples;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class NettyServer implements Runnable {
    private int port;

    public enum MessageFormat {
        BYTES,
        STRING
    }

    public NettyServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            /**
             * 确定消息格式(byte/string)、是否使用SSL的组合方式
             * 1 byte & SSL
             * 2 byte & No SSL
             * 3 string & SSL
             * 4 string & No SSL
             */
            int type = 1;
            SslContext sslCtx = null;
            MessageFormat messageFormat;
            if (type < 3) {
                messageFormat = MessageFormat.BYTES;
            } else {
                messageFormat = MessageFormat.STRING;
            }
            if ((type % 2) == 1) {
                SelfSignedCertificate ssc = new SelfSignedCertificate();
                sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            }

            runNettyServer(messageFormat, sslCtx);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void runNettyServer(MessageFormat messageFormat, SslContext sslContext) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new SimpleServerHandler())
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //在这里加入处理读写的handler
                            ChannelPipeline pipeline = ch.pipeline();
                            if (sslContext != null) {
                                // SSL自签名证书
                                pipeline.addLast(sslContext.newHandler(ch.alloc()));
                            }
                            if (messageFormat == MessageFormat.STRING) {
                                pipeline.addLast(new StringEncoder(StandardCharsets.UTF_8));
                                pipeline.addLast(new StringDecoder(StandardCharsets.UTF_8));
                            }
                            pipeline.addLast(new ServerHandler());
                        }
                    });
            ChannelFuture f = b.bind(this.port).sync();
            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private class ServerHandler extends ChannelInboundHandlerAdapter {
        /**
         * 如果线程堵塞，会一次收到客户端多次发送的信息
         * <p>
         * 添加本地变量lastBuf，由于存放线程堵塞时，上一次连接剩余的字节数组
         */
        private ByteBuf lastBuf = null;

        private static final int fixMessageLength = 36;

        private Logger logger = LoggerFactory.getLogger(ServerHandler.class);

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            logger.info("服务端通道激活：" + ctx.channel().localAddress());
            String ack = "服务端连接成功!";
            ctx.writeAndFlush(Unpooled.wrappedBuffer(ack.getBytes(StandardCharsets.UTF_8))); // 必须有flush
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf) {
                // 接收到字节数组后,解码成字符串，返回编码成字节数组的确认信息
                if (lastBuf == null) {
                    lastBuf = (ByteBuf) msg;
                } else {
                    lastBuf = Unpooled.wrappedBuffer(lastBuf, (ByteBuf) msg);
                }
                // 每次<b>固定读取</b>一个消息长度的字节，剩余未读取的字节留到下次接收到客户端传输时读取
                byte[] bytes = new byte[fixMessageLength];
                while (lastBuf.readableBytes() > fixMessageLength) {
                    lastBuf.readBytes(bytes);
                    String receiveMessage = new String(bytes);
                    logger.info("服务端收到: " + receiveMessage);
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(receiveMessage.getBytes(StandardCharsets.UTF_8)));
                }
            } else if (msg instanceof String) {
                logger.info("服务端收到: " + msg);
                ctx.writeAndFlush(msg);
            }
        }
    }

    private class SimpleServerHandler extends ChannelInboundHandlerAdapter {
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