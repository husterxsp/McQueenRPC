package me.bluedavy.mcqueen.rpc.netty4.server;
/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import me.bluedavy.mcqueen.rpc.NamedThreadFactory;
import me.bluedavy.mcqueen.rpc.ProtocolFactory;
import me.bluedavy.mcqueen.rpc.netty4.serialize.NettyProtocolDecoder;
import me.bluedavy.mcqueen.rpc.netty4.serialize.NettyProtocolEncoder;
import me.bluedavy.mcqueen.rpc.server.Server;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Netty Server
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class NettyServer implements Server {

    private static final Log LOGGER = LogFactory.getLog(NettyServer.class);
    private static final int PROCESSORS = Runtime.getRuntime().availableProcessors();
    private ServerBootstrap bootstrap = null;
    private AtomicBoolean startFlag = new AtomicBoolean(false);

    public NettyServer() {
        ThreadFactory serverBossTF = new NamedThreadFactory("NETTYSERVER-BOSS-");
        ThreadFactory serverWorkerTF = new NamedThreadFactory("NETTYSERVER-WORKER-");
        EventLoopGroup bossGroup = new NioEventLoopGroup(PROCESSORS, serverBossTF);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup(PROCESSORS * 2, serverWorkerTF);
        workerGroup.setIoRatio(Integer.parseInt(System.getProperty("nfs.rpc.io.ratio", "50")));
        bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.SO_REUSEADDR, Boolean.parseBoolean(System.getProperty("nfs.rpc.tcp.reuseaddress", "true")))
                .option(ChannelOption.TCP_NODELAY, Boolean.parseBoolean(System.getProperty("nfs.rpc.tcp.nodelay", "true")));
    }

    public void start(int listenPort, final ExecutorService threadPool) throws Exception {
        if (!startFlag.compareAndSet(false, true)) {
            return;
        }
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {

            protected void initChannel(SocketChannel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", new NettyProtocolDecoder());
                pipeline.addLast("encoder", new NettyProtocolEncoder());
                pipeline.addLast("handler", new NettyServerHandler(threadPool));
            }

        });
        bootstrap.bind(new InetSocketAddress(listenPort)).sync();
        LOGGER.warn("Server started,listen at: " + listenPort);
    }

    /**
     * 注册业务处理器
     * @param protocolType 协议类型
     * @param serviceName 服务名
     * @param serviceInstance 服务实例
     */
    public void registerProcessor(int protocolType, String serviceName, Object serviceInstance) {
        ProtocolFactory.getServerHandler(protocolType).registerProcessor(serviceName, serviceInstance);
    }

    public void stop() throws Exception {
        LOGGER.warn("Server stop!");
        startFlag.set(false);
    }

}
