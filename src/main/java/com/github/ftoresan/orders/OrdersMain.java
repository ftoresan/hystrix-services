package com.github.ftoresan.orders;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
public class OrdersMain {

    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(OrdersConfiguration.class);
    }


}
