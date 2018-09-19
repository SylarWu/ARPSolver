package funcarose;

import funcarose.controller.DeviceAccess;

import static java.lang.Thread.sleep;

public class main {
    static volatile boolean test = true;
    static long before = System.currentTimeMillis();
    static long after;

    public static void main(String args[]){
        long i = Long.MIN_VALUE;

        int count = 0;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                after = System.currentTimeMillis();

                while(after - before <1000){
                    System.out.println(after - before);
                }
                test = false;

            }
        });
        t.start();


        while(test){
            i++;
            if (i == 0){
                count++;
            }
        }
        System.out.println("i:" + i);
        System.out.println("count:" + count);
    }
}
