package com.hutu;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        double r = ThreadLocalRandom.current().nextDouble();
        System.out.println(r);
    }
}
