package com.example.download;

import java.util.concurrent.LinkedBlockingQueue;

public class Test {
	public static void main(String[] args) {
	    final LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
	 
	    new Thread() {
	        @Override
	        public void run() {
	            while(true) {
	                try {
	                    Thread.sleep(1000);
	                    queue.add(new Runnable() {
	                        @Override
	                        public void run() {
	                            System.out.println("从子线程提交的任务!!" + Thread.currentThread().getName());
	                        }
	                    });
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	            }
	        }
	    }.start();
	 
	    while (true) {
	        try {
	            Runnable r = queue.take();
	            System.out.println("主线程执行子线程命令: " + Thread.currentThread().getName());
	            r.run();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	 
	    }
	}
}
