package cn.chinatelecom.www;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

class Car{
    private volatile boolean waxOn = false;
    private ReentrantLock lock= new ReentrantLock();
    private Condition condition = lock.newCondition();
    public synchronized void waxed(){
        waxOn = true;
        System.out.println("Wax on");
        notifyAll();
    }
    public void waxed1(){
        lock.lock();
        try{
            waxOn = true;
            System.out.println("Wax on");
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
    public void buffed1(){
        lock.lock();
        try{
            waxOn = false;
            System.out.println("Buffed");
            condition.signalAll();
        }finally {
            lock.unlock();
        }
    }
    public synchronized void buffed(){
        waxOn = false;
        System.out.println("Buffed");
        notifyAll();
    }
    public void waitforwaxing1() throws InterruptedException{
        lock.lock();
        try {
            while (waxOn == false)
                condition.await();
        }finally {
            lock.unlock();
        }
    }
    public void waitforbuffing1() throws InterruptedException{
        lock.lock();
        try {
            while (waxOn == true)
                condition.await();
        }finally{
            lock.unlock();
        }
    }
    public synchronized void waitforwaxing() throws InterruptedException {
        while (waxOn == false)
            wait();
    }
    public synchronized void waitforbuffing() throws InterruptedException{
        while (waxOn == true)
            wait();
    }
}

class WaxOn implements Runnable{
    private Car car;
    public WaxOn(Car c){
        car = c;
    }
    @Override
    public void run() {
        try {
        while (!Thread.interrupted()){

                //car.waxed();
                car.waxed1();
                TimeUnit.MICROSECONDS.sleep(2000);

                //car.waitforbuffing();
                car.waitforbuffing1();


        }
        }catch (InterruptedException e){
            System.out.println("exit wax via interruption");
        }
        System.out.println("Ending wax on task");
    }
}

class WaxOff implements Runnable{
    private Car car;
    public WaxOff(Car c){
        car = c;
    }
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                    //car.waitforwaxing();
                    car.waitforwaxing1();
                    TimeUnit.MICROSECONDS.sleep(2000);
                    //car.buffed();
                    car.buffed1();







            }
        }catch (InterruptedException e){
            System.out.println("exit buffing via interruption");
        }
        System.out.println("Ending buffing on task");
    }
}
public class WaitAndNotify {
    public static void main(String[] args) throws Exception {
        ExecutorService exec = Executors.newCachedThreadPool();
        Car c = new Car();
        exec.execute(new WaxOn(c));
        exec.execute(new WaxOff(c));
        TimeUnit.SECONDS.sleep(2);
        exec.shutdownNow();
    }
}
