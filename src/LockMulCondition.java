import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.LinkedList;
import java.util.List;
 
public class LockMulCondition {
    public static void main(String arg[]) {
 
        Buffer buffer=new Buffer();
        Producer producer=new Producer(buffer);
        Consumer consumer=new Consumer(buffer);
        //创建线程执行生产和消费
        new Thread(consumer,"consumer").start();
 
        new Thread(producer,"producer").start();
 
    }
 
    public static class Consumer implements Runnable {
        private Buffer buffer;
        public Consumer(Buffer buffer) {
            this.buffer = buffer;
        }
 
        public void run() {
            for (Integer i=0; i<5; i++)
                buffer.get();
        }
 
    }
 
    public static class Producer implements Runnable {
        private Buffer buffer;
        public Producer(Buffer buffer) {
            this.buffer = buffer;
        }
 
        public void run() {
            for (Integer i=0; i<5; i++)
                buffer.put(Thread.currentThread().getName());
        }
 
    }
 
    public static class Buffer {
        private ReentrantLock reentrantLock;
        private Condition full;
        private Condition empty;
        private Integer size = 1;
        private AtomicInteger userd = new AtomicInteger(0);
        private AtomicInteger num = new AtomicInteger(0);
        private LinkedList<String> data;
 
        public Buffer() {
            reentrantLock = new ReentrantLock();
            full = reentrantLock.newCondition();
            empty = reentrantLock.newCondition();
            data = new LinkedList<>();
        }
 
        public void put(String string){
            reentrantLock.lock();
            try {
                while (userd.get() == size) {
                    full.await();
                }
                data.addLast(string);
                userd.getAndIncrement();
                num.getAndIncrement();
                System.out.println("" + num.intValue() + Thread.currentThread().getName());
                empty.signalAll();
 
            }catch (InterruptedException exception) {
                System.out.println(Thread.currentThread().getName() + "中断异常");
            }finally {
                reentrantLock.unlock();
            }
        }
 
        public void get() {
            reentrantLock.lock();
            try {
 
                while (userd.get() == 0) {
                    empty.await();
                }
                String string = data.removeFirst();
                userd.getAndDecrement();
                num.getAndIncrement();
                System.out.println("" + num.intValue() + Thread.currentThread().getName());
                full.signalAll();
 
 
            }catch (InterruptedException exception) {
                System.out.println(Thread.currentThread().getName() + "中断异常");
            }finally {
                reentrantLock.unlock();
            }
        }
 
    }
}
