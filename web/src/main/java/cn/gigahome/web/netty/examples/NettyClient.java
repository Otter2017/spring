package cn.gigahome.web.netty.examples;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        try {
            /**
             * 确定消息格式(byte/string)、是否使用SSL的组合方式
             * 1 byte & SSL
             * 2 byte & No SSL
             * 3 string & SSL
             * 4 string & No SSL
             */
            int type = 3;
            SslContext sslCtx = null;
            NettyServer.MessageFormat messageFormat;
            if (type < 3) {
                messageFormat = NettyServer.MessageFormat.BYTES;
            } else {
                messageFormat = NettyServer.MessageFormat.STRING;
            }
            if ((type % 2) == 1) {
                sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
            }

            runNettyClient(messageFormat, sslCtx);
        } catch (Exception ex) {

        }
    }

    private static void runNettyClient(NettyServer.MessageFormat messageFormat, SslContext sslContext) {
        try {
            Bootstrap bootstrap = new Bootstrap();
            NioEventLoopGroup group = new NioEventLoopGroup();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer() {
                        @Override
                        protected void initChannel(Channel ch) {
                            if (sslContext != null) {
                                ch.pipeline().addLast(sslContext.newHandler(ch.alloc(), "192.168.43.196", 1884));
                            }
                            if (NettyServer.MessageFormat.STRING == messageFormat) {
                                ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                                ch.pipeline().addLast(new MessageDecorator("#"));
                                ch.pipeline().addLast(new MessageDecorator("*"));
                                ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                            }
                            ch.pipeline().addLast(new ClientHandler());
                        }
                    });

            Channel channel = bootstrap.connect("192.168.43.196", 1884).channel();
            Random random = new Random();
            while (true) {
                //将发送的信息编码成字节数组发送给服务端，isActive必需(SSL握手可能耗时比较长，会执行一到两次sleep)
                if (channel.isActive() && channel.isOpen()) {
                    String message = UUID.randomUUID().toString();
                    if (NettyServer.MessageFormat.STRING == messageFormat) {
                        channel.writeAndFlush(message);
                    } else if (NettyServer.MessageFormat.BYTES == messageFormat) {
                        channel.writeAndFlush(Unpooled.wrappedBuffer(message.getBytes(StandardCharsets.UTF_8)));
                    }
                    System.out.println("send " + message);
                }
                int sleepInterval = random.nextInt(200);
                Thread.sleep(sleepInterval);
            }
        } catch (Exception ex) {

        }
    }

    private static class ClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("已连接到:" + ctx.channel().remoteAddress());
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof String) {
                System.out.println("客户端收到:" + msg);
            } else {
                ByteBuf byteBuf = (ByteBuf) msg;
                int readLength = byteBuf.readableBytes();
                byte[] bytes = new byte[readLength];
                byteBuf.readBytes(bytes);
                String receiveMessage = new String(bytes);
                System.out.println("客户端收到:" + receiveMessage);
            }
        }
    }

    static class MessageDecorator extends ChannelOutboundHandlerAdapter {
        private String decorateChar;

        public MessageDecorator(String decorateChar) {
            this.decorateChar = decorateChar;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            System.out.println("message add decorateChar");
            msg = this.decorateChar + msg + this.decorateChar;
            super.write(ctx, msg, promise);
        }
    }
}
