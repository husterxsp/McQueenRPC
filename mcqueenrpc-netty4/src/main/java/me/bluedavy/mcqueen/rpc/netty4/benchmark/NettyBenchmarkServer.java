package me.bluedavy.mcqueen.rpc.netty4.benchmark;
/**
 * nfs-rpc
 *   Apache License
 *   
 *   http://code.google.com/p/nfs-rpc (c) 2011
 */
import me.bluedavy.mcqueen.rpc.benchmark.AbstractBenchmarkServer;
import me.bluedavy.mcqueen.rpc.netty4.server.NettyServer;
import me.bluedavy.mcqueen.rpc.server.Server;

/**
 * Netty RPC Benchmark Server
 * 
 * @author <a href="mailto:bluedavy@gmail.com">bluedavy</a>
 */
public class NettyBenchmarkServer extends AbstractBenchmarkServer {


	/**
	 * Benchmark配置：端口、线程数、response大小
	 * run方法，启动server
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{

		new NettyBenchmarkServer().run(args);
	}

	/**
	 * netty server 配置
	 * @return
	 */
	public Server getServer() {
		return new NettyServer();
	}

}
