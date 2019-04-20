package me.bluedavy.mcqueen.rpc.netty4.server;
/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.bluedavy.mcqueen.rpc.ProtocolFactory;
import me.bluedavy.mcqueen.rpc.RequestWrapper;
import me.bluedavy.mcqueen.rpc.ResponseWrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Netty Server Handler
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    private static final Log LOGGER = LogFactory.getLog(NettyServerHandler.class);

    private ExecutorService threadpool;

    public NettyServerHandler(ExecutorService threadpool) {
        this.threadpool = threadpool;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
            throws Exception {
        if (!(e.getCause() instanceof IOException)) {
            // only log
            LOGGER.error("catch some exception not IOException", e.getCause());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (!(msg instanceof RequestWrapper) && !(msg instanceof List)) {
            LOGGER.error("receive message error,only support RequestWrapper || List");
            throw new Exception(
                    "receive message error,only support RequestWrapper || List");
        }
        handleRequest(ctx, msg);
    }

    @SuppressWarnings("unchecked")
    private void handleRequest(final ChannelHandlerContext ctx, final Object message) {
        try {
            // 这个地方为啥要用线程池呢？
            // 耗时的业务操作应该放在线程池里执行。有两种方式
            // 1. channelRead里手动添加线程池操作，就向下面所示的代码
            // 2. pipeline.addLast的时候，设置一个业务逻辑 EventLoopFroup
            // 参考：
            // https://www.jianshu.com/p/727bbc7454dc
            // https://huanglei.rocks/coding/how-to-execute-business-login-in-netty.html

            threadpool.execute(new HandlerRunnable(ctx, message, threadpool));
        } catch (RejectedExecutionException exception) {
            LOGGER.error("server threadpool full,threadpool maxsize is:"
                    + ((ThreadPoolExecutor) threadpool).getMaximumPoolSize());
            if (message instanceof List) {
                List<RequestWrapper> requests = (List<RequestWrapper>) message;
                for (final RequestWrapper request : requests) {
                    sendErrorResponse(ctx, request);
                }
            } else {
                sendErrorResponse(ctx, (RequestWrapper) message);
            }
        }
    }

    private void sendErrorResponse(final ChannelHandlerContext ctx, final RequestWrapper request) {
        ResponseWrapper responseWrapper = new ResponseWrapper(request.getId(), request.getCodecType(), request.getProtocolType());
        responseWrapper
                .setException(new Exception("server threadpool full,maybe because server is slow or too many requests"));
        ChannelFuture wf = ctx.channel().writeAndFlush(responseWrapper);
        wf.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    LOGGER.error("server write response error,request id is: " + request.getId());
                }
            }
        });
    }

    class HandlerRunnable implements Runnable {

        private ChannelHandlerContext ctx;

        private Object message;

        private ExecutorService threadPool;

        public HandlerRunnable(ChannelHandlerContext ctx, Object message, ExecutorService threadPool) {
            this.ctx = ctx;
            this.message = message;
            this.threadPool = threadPool;
        }

        @Override
        @SuppressWarnings("rawtypes")
        public void run() {
            // pipeline
            if (message instanceof List) {
                List messages = (List) message;
                for (Object messageObject : messages) {
                    threadPool.execute(new HandlerRunnable(ctx, messageObject, threadPool));
                }
            } else {
                RequestWrapper request = (RequestWrapper) message;
                long beginTime = System.currentTimeMillis();
                ResponseWrapper responseWrapper = ProtocolFactory.getServerHandler(request.getProtocolType()).handleRequest(request);
                final int id = request.getId();
                // already timeout,so not return
                if ((System.currentTimeMillis() - beginTime) >= request.getTimeout()) {
                    LOGGER.warn("timeout,so give up send response to client,requestId is:"
                            + id
                            + ",client is:"
                            + ctx.channel().remoteAddress() + ",consumetime is:" + (System.currentTimeMillis() - beginTime) + ",timeout is:" + request.getTimeout());
                    return;
                }
                ChannelFuture wf = ctx.channel().writeAndFlush(responseWrapper);
                wf.addListener(new ChannelFutureListener() {
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (!future.isSuccess()) {
                            LOGGER.error("server write response error,request id is: " + id);
                        }
                    }
                });
            }
        }

    }

}
