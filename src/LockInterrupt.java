import java.util.concurrent.locks.ReentrantLock;
 
public class LockInterrupt {
    public static void main(String args[]) throws InterruptedException {
        Resource resource = new Resource();
        Thread o1 = new Thread(new Operation(resource), "o1");
        Thread o2 = new Thread(new Operation(resource), "o2");
        o1.start();
        Thread.sleep(200);
        o2.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Long startTime = System.currentTimeMillis();
                while ((System.currentTimeMillis() - startTime) <= 1000) {
                    continue;
                }
                o2.interrupt();
            }
        }).start();
    }
 
    public static class Operation implements Runnable {
        private Resource resource;
        public Operation(Resource resource) {
            this.resource = resource;
        }
 
        public void run() {
            try {
                resource.operation();
            }catch (InterruptedException exception) {
                System.out.println(Thread.currentThread().getName()+"被中断了啊");
            }
        }
 
    }
 
    public static class Resource {
        private ReentrantLock reentrantLock = new ReentrantLock();
 
        public void operation() throws InterruptedException{
            reentrantLock.lockInterruptibly();
            try {
                //xx操作
                System.out.println(Thread.currentThread().getName()+"操作开始");
                Thread.sleep(3000);
            }finally {
                reentrantLock.unlock();
                System.out.println(Thread.currentThread().getName()+"操作完成");
            }
        }
 
    }
}
