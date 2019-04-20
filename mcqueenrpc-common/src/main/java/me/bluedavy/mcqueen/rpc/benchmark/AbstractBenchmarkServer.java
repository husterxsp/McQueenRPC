package me.bluedavy.mcqueen.rpc.benchmark;
/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */

import com.esotericsoftware.kryo.serializers.DefaultArraySerializers;
import com.google.protobuf.ByteString;
import me.bluedavy.mcqueen.rpc.NamedThreadFactory;
import me.bluedavy.mcqueen.rpc.protocol.PBDecoder;
import me.bluedavy.mcqueen.rpc.protocol.RPCProtocol;
import me.bluedavy.mcqueen.rpc.protocol.SimpleProcessorProtocol;
import me.bluedavy.mcqueen.rpc.server.Server;
import me.bluedavy.mcqueen.rpc.server.ServerProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;


/**
 * Abstract benchmark server
 * <p>
 * Usage: BenchmarkServer listenPort maxThreads responseSize
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public abstract class AbstractBenchmarkServer {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss");

    public void run(String[] args) throws Exception {
        if (args == null || args.length != 3) {
            throw new IllegalArgumentException(
                    "must give three args: listenPort | maxThreads | responseSize");
        }
        int listenPort = Integer.parseInt(args[0]);
        int maxThreads = Integer.parseInt(args[1]);
        final int responseSize = Integer.parseInt(args[2]);
        System.out.println(dateFormat.format(new Date())
                + " ready to start server,listenPort is: " + listenPort
                + ",maxThreads is:" + maxThreads + ",responseSize is:"
                + responseSize + " bytes");


        Server server = getServer();
        server.registerProcessor(SimpleProcessorProtocol.TYPE, RequestObject.class.getName(), new ServerProcessor() {
            @Override
            public Object handle(Object request) throws Exception {
                // 简单处理，即对于任何请求，直接返回new一个对象返回。
                return new ResponseObject(responseSize);
            }
        });

        // for pb codec
        PBDecoder.addMessage(PB.RequestObject.class.getName(), PB.RequestObject.getDefaultInstance());
        PBDecoder.addMessage(PB.ResponseObject.class.getName(), PB.ResponseObject.getDefaultInstance());
        server.registerProcessor(SimpleProcessorProtocol.TYPE, PB.RequestObject.class.getName(), new ServerProcessor() {
            @Override
            public Object handle(Object request) throws Exception {
                PB.ResponseObject.Builder builder = PB.ResponseObject.newBuilder();
                builder.setBytesObject(ByteString.copyFrom(new byte[responseSize]));
                return builder.build();
            }
        });
        server.registerProcessor(RPCProtocol.TYPE, "testservice", new BenchmarkTestServiceImpl(responseSize));
        server.registerProcessor(RPCProtocol.TYPE, "testservicepb", new PBBenchmarkTestServiceImpl(responseSize));

        KryoUtils.registerClass(byte[].class, new DefaultArraySerializers.ByteArraySerializer(), 0);
        KryoUtils.registerClass(RequestObject.class, new RequestObjectSerializer(), 1);
        KryoUtils.registerClass(ResponseObject.class, new ResponseObjectSerializer(), 2);

        ThreadFactory tf = new NamedThreadFactory("BUSINESSTHREADPOOL");
        ExecutorService threadPool = new ThreadPoolExecutor(20, maxThreads,
                300, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), tf);
        server.start(listenPort, threadPool);
    }

    /**
     * Get server instance
     */
    public abstract Server getServer();

}
