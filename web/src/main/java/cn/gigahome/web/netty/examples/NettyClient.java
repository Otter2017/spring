package cn.gigahome.web.netty.examples;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

public class NettyClient {
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();
        MessageDecorator firstDecorator = new MessageDecorator();
        firstDecorator.setDecorateChar("#");
        MessageDecorator lastDecorator = new MessageDecorator();
        lastDecorator.setDecorateChar("*");
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(new StringEncoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(firstDecorator);
                        ch.pipeline().addLast(lastDecorator);
                        ch.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                        ch.pipeline().addLast(new ClientHandler());
                        ch.pipeline().addLast(new ByteArrayEncoder());
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                    }
                });

        Channel channel = bootstrap.connect("localhost", 8080).channel();
        Random random = new Random();
        while (true) {
            String message = UUID.randomUUID().toString();
            channel.writeAndFlush(message);
            System.out.println("send " + message);
            Thread.sleep(random.nextInt(2000));
        }
    }

    private static class ClientHandler extends SimpleChannelInboundHandler<String> {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            String sendInfo = "Hello 这里是客户端  你好啊！";
            ctx.writeAndFlush(sendInfo); // 必须有flush
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            System.out.println("客户端收到:" + msg);
        }
    }

    static class MessageDecorator extends ChannelOutboundHandlerAdapter {
        private String decorateChar;

        private void setDecorateChar(String decorateChar) {
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
