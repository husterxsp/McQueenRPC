package me.bluedavy.mcqueen.rpc.netty4.benchmark;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 死循环
 * http://hellojava.info/?tag=thread-interrupt
 * 两种解决办法：
 * 1.忽略掉异常，尽管通常不建议这么做，因为通常调interrupt的地方应该是希望中断线程的处理；
 * 2. 包装成一个checked exception，交由上层来处理
 */
public class Test {

    private ReentrantLock lock = new ReentrantLock();

    private Condition condition = lock.newCondition();

    public static void main(String[] args) throws Exception {
        Thread t = new Thread(new Runnable() {
            public void run() {
                Test test = new Test();
                test.say();
            }
        }, "thread1-0");
        t.start();
        t.interrupt();
        for (int i = 0; i < 10; i++) {
            new Thread(new Runnable() {
                public void run() {
                    Test test = new Test();
                    test.say();
                }
            }, "thread2-" + i).start();
        }
    }

    public void say() {
        try {
            lock.lock();
            while (true) {
                try {
                    condition.await();
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }
        } catch (Exception e) {
            System.out.println("2" + e);
        } finally {
            lock.unlock();
        }
    }

}
