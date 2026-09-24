package com.interview.lld;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {

    static class Counter {
        static AtomicInteger count = new AtomicInteger(0);

        static void increment() {
            count.incrementAndGet();
        }

        static int getCount() {
            return count.get();
        }
    }

    // count++ -> read count, add 1 then write

    public static void main(String[] args) throws InterruptedException, ExecutionException {

        Runnable task = () -> {
            Counter.increment();
            System.out.println("Running in: " + Thread.currentThread().getName());
        };

        Thread[] threads = new Thread[1000];
        // Start all 1000 threads

        for (int i = 0; i < 1000; i++) {
            threads[i] = new Thread(task, "worker-" + i);
            threads[i].start();
        }

        // Wait for ALL threads to finish
        for (Thread thread : threads) {
            thread.join();
        }
        // schedules the thread to run

        // t.run(); // runs synchronously

        // main blocks here until t finishes
        System.out.println("Final Count: " + Counter.getCount());

        System.out.println("Running in: " + Thread.currentThread().getName());

        ExecutorService executor = Executors.newFixedThreadPool(4);

        Future<Integer> future = executor.submit(() -> {
            System.out.println("this is a thread");
            Thread.sleep(1000);
            return 42;
        });

        int result = future.get();

        System.out.println(result);

        executor.shutdown();
    }
}



/*
Thread lifecycle
    NEW
    RUNNABLE
    TIMED_WAITING
    WAITING/BLOCKED
    TERMINATED

Deadlock: can be avoided using lock ordering

*/