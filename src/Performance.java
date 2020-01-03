import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * cas, reentrantlock, synchronized 性能对比
 * 不同数量线程争用时，所有线程运行完成耗时，单位毫秒：
 *
 * 线程数为200：
 *
 *     Synchronized：9
 *
 *     reentrantLock：8
 *
 *     cas                 ：4
 *
 *
 *
 * 线程数为2000：
 *
 *     Synchronized：189
 *
 *     reentrantLock：156
 *
 *     cas                 ：140
 *
 *
 *
 * 线程数为20000：
 *
 *     Synchronized：1657
 *
 *     reentrantLock：2126
 *
 *     cas                 ：3013
 */
public class Performance {
    private Increment increment;
    private Integer num;
    private CyclicBarrier endcyclicBarrier;
    private CyclicBarrier startCyclicBarrier;
    private Long start;

    public Performance(Integer num) {
        this.num = num;
        this.endcyclicBarrier = new CyclicBarrier(num, new Runnable() {
            @Override
            public void run() {
                Long end = System.currentTimeMillis();
                System.out.println("所有线程结束，开始完成，时间：" + end);

                System.out.println("用时：" + (end-start));
                System.out.println("最终值：" + increment.getValue());
            }
        });
        this.startCyclicBarrier = new CyclicBarrier(num, new Runnable() {
            @Override
            public void run() {
                start = System.currentTimeMillis();
                System.out.println("所有线程就绪，开始执行，时间：" + start);
            }
        });
        //this.increment = new SynchronizedInc(startCyclicBarrier, endcyclicBarrier);
        this.increment = new ReentrantInc(startCyclicBarrier, endcyclicBarrier);
        //this.increment = new CasInc(startCyclicBarrier, endcyclicBarrier);

    }

    public static void main(String arg[]) {
        new Performance(10000).test();
        return;
    }

    public void test() {
        for (Integer i = 0; i < num; i ++ ) {
            new Thread(increment, "thread" + i).start();
        }

    }

    public static abstract class Increment implements Runnable{
        public Integer i = 0;
        public abstract void inc();
        public abstract Integer getValue();
        public CyclicBarrier startCyclicBarrier;
        public CyclicBarrier endCyclicBarrier;

        public Increment(CyclicBarrier startCyclicBarrier, CyclicBarrier endCyclicBarrier) {
            this.startCyclicBarrier = startCyclicBarrier;
            this.endCyclicBarrier = endCyclicBarrier;
        }

        @Override
        public void run() {
            try {
                startCyclicBarrier.await();//控制统一开始
                inc();
                endCyclicBarrier.await();//登记完成
            }catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + "异常");
            }
        }
    }

    public static class SynchronizedInc extends Increment {
        public SynchronizedInc(CyclicBarrier startCyclicBarrier, CyclicBarrier endCyclicBarrier) {
            super(startCyclicBarrier, endCyclicBarrier);
        }


        @Override
        public synchronized void inc() {
            i++;

        }

        @Override
        public Integer getValue() {
            return i;
        }
    }

    public static class ReentrantInc extends Increment {
        private ReentrantLock lock;

        public ReentrantInc(CyclicBarrier startCyclicBarrier, CyclicBarrier endCyclicBarrier) {
            super(startCyclicBarrier, endCyclicBarrier);
            this.lock = new ReentrantLock();
        }

        @Override
        public void inc() {
            lock.lock();
            try {
                i++;
            }finally {
                lock.unlock();
            }
        }

        @Override
        public Integer getValue() {
            return i;
        }
    }

    public static class CasInc extends Increment {
        private AtomicInteger integer = new AtomicInteger(0);

        public CasInc(CyclicBarrier startCyclicBarrier, CyclicBarrier endCyclicBarrier) {
            super(startCyclicBarrier, endCyclicBarrier);
        }

        @Override
        public void inc() {
            integer.getAndIncrement();
        }

        @Override
        public Integer getValue() {
            return integer.intValue();
        }
    }

}
