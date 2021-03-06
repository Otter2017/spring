import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.UUID;
import java.util.concurrent.*;

public class NIOTest {
    private static Logger logger = LoggerFactory.getLogger(NIOTest.class);
    private static final int BUF_SIZE = 1024;

    private static final int PORT = 8080;

    public static void main(String[] args) {
        ExecutorService serverService = Executors.newSingleThreadExecutor(r -> {
            Thread newThread = new Thread(r);
            newThread.setDaemon(true);
            newThread.setName("SERVER");
            return newThread;
        });
        Server server = new Server();
        serverService.submit(server);

        CountDownLatch countDownLatch = new CountDownLatch(5);
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Client(countDownLatch), 2, 5, TimeUnit.SECONDS);
        try {
            countDownLatch.await();
            logger.info("exit");
            scheduledExecutorService.shutdown();
            serverService.shutdown();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    static class Server implements Runnable {

        private Selector selector;

        private ServerSocketChannel serverSocketChannel;

        private void init() {
            try {
                selector = Selector.open();
                serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.socket().bind(new InetSocketAddress(PORT));
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (Exception ex) {
                logger.info("Server init failure:{}", ex.getMessage());
            }
        }

        Server() {
            init();
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (serverSocketChannel != null) {
                    serverSocketChannel.close();
                }
            } catch (IOException e) {
                logger.error("finalize server error");
            }
            super.finalize();
        }

        @Override
        public void run() {
            logger.info("server running...");
            try {
                while (selector.select() > 0) {
                    //堵塞，直到有客户端连接
                    Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                    while (selectionKeys.hasNext()) {
                        SelectionKey key = selectionKeys.next();
                        if (key.isAcceptable()) {
                            handleAccept(key);
                        }
                        if (key.isReadable()) {
                            handleRead(key, true);
                        }
                        //处理完之后需主动移除
                        selectionKeys.remove();
                    }
                }
            } catch (Exception ex) {
                logger.info("selector error");
            }
        }
    }

    static class Client implements Runnable {
        private CountDownLatch countDownLatch;

        private SocketChannel socketChannel;

        private Selector selector;

        Client(CountDownLatch countDownLatch) {
            init();
            this.countDownLatch = countDownLatch;
        }

        private void init() {
            try {
                socketChannel = SocketChannel.open(new InetSocketAddress("localhost", PORT));
                socketChannel.configureBlocking(false);
                selector = Selector.open();
                socketChannel.register(selector, SelectionKey.OP_READ);
            } catch (Exception ex) {
                logger.error("client init failure");
            }
        }

        private void sendMessage(String message) {
            try {
                ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
                buffer.put(message.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                socketChannel.write(buffer);
                buffer.compact();
                logger.info("client send message : {}", message);
            } catch (Exception ex) {
                logger.error("send message error:{}", ex.getMessage());
            }
        }

        @Override
        public void run() {
            try {
                String uuid = UUID.randomUUID().toString();
                String message = uuid + " from " + socketChannel.getLocalAddress();
                sendMessage(message);
                selector.select();
                Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
                while (selectionKeys.hasNext()) {
                    SelectionKey key = selectionKeys.next();
                    if (key.isReadable()) {
                        handleRead(key, false);
                        countDownLatch.countDown();
                    }
                    selectionKeys.remove();
                }
            } catch (Exception ex) {
                logger.error("client run error");
            }
        }

        @Override
        protected void finalize() throws Throwable {
            try {
                if (selector != null) {
                    selector.close();
                }
                if (socketChannel != null) {
                    socketChannel.close();
                }
            } catch (Exception ex) {
                logger.error("finalize client error");
            }
            super.finalize();
        }
    }

    private static void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel ssChannel = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssChannel.accept();
        logger.info("accept connection from {}", sc.getRemoteAddress());
        sc.configureBlocking(false);
        //注册读
        sc.register(key.selector(), SelectionKey.OP_READ);
    }

    private static synchronized void handleRead(SelectionKey key, boolean ack) throws IOException {
        SocketChannel sc = (SocketChannel) key.channel();
        logger.info("read from {}", sc.getRemoteAddress().toString());
        ByteBuffer buf = ByteBuffer.allocateDirect(BUF_SIZE);
        if (sc.isConnected()) {
            int bytesRead = sc.read(buf);
            logger.info("bytes count {}", bytesRead);
            while (bytesRead > 0) {
                byte[] content = new byte[bytesRead];
                buf.flip();
                while (buf.hasRemaining()) {
                    buf.get(content);
                    logger.info("receive: {}", new String(content, StandardCharsets.UTF_8));
                }
                buf.clear();
                bytesRead = sc.read(buf);
            }
            if (ack) {
                sc.write(ByteBuffer.wrap("ACK".getBytes()));
            }
            if (bytesRead == -1) {
                sc.close();
            }
        }
    }


}