package me.bluedavy.mcqueen.rpc.benchmark;
/**
 * nfs-rpc
 * Apache License
 * <p>
 * http://code.google.com/p/nfs-rpc (c) 2011
 */

import me.bluedavy.mcqueen.rpc.client.ClientFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

/**
 * Test for RPC based on direct call Benchmark
 *
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public abstract class AbstractSimpleProcessorBenchmarkClient extends AbstractBenchmarkClient {

    @Override
    public ClientRunnable getClientRunnable(String targetIP, int targetPort,
                                            int clientNums, int rpcTimeout, int dataType, int requestSize,
                                            CyclicBarrier barrier, CountDownLatch latch, long endTime, long startTime) {
        return new SimpleProcessorBenchmarkClientRunnable(
                getClientFactory(), targetIP, targetPort,
                clientNums, rpcTimeout, dataType, requestSize, barrier, latch,
                startTime, endTime);
    }

    public abstract ClientFactory getClientFactory();

}
