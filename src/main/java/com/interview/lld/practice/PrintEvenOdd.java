package com.interview.lld.practice;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PrintEvenOdd {
    int number = 1;
    int limit;

    PrintEvenOdd(int limit) {
        this.limit = limit;
    }

    synchronized void printEven() throws InterruptedException {

        while (number <= limit) {

            // Even thread waits when number is odd
            while (number % 2 == 1) {
                wait();
            }

            if (number <= limit) {
                System.out.println("Thread Even: " + number);
                number++;

                // Wake up odd thread
                notify();
            }
        }
    }

    synchronized void printOdd() throws InterruptedException {

        while (number <= limit) {

            // Odd thread waits when number is even
            while (number % 2 == 0) {
                wait();
            }

            if (number <= limit) {
                System.out.println("Thread Odd: " + number);
                number++;

                // Wake up even thread
                notify();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {

        PrintEvenOdd printEvenOdd = new PrintEvenOdd(100);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

//        Thread t1 = new Thread(() -> {
//            try {
//                printEvenOdd.printEven();
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        });

        executorService.submit(() -> {
            try {
                printEvenOdd.printEven();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executorService.submit(() -> {
            try {
                printEvenOdd.printOdd();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        executorService.shutdown();


        ReadWriteLock lock = new ReentrantReadWriteLock();

//        t1.start();
//        t2.start();
//
//        t1.join();
//        t2.join();
    }
}