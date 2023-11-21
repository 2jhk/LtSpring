package com.lt.service;

import com.lt.spring.LtApplicationContext;

public class Test {
    public static void main(String[] args) {
        ///
        LtApplicationContext ltApplicationContext = new LtApplicationContext(AppConfig.class);
        System.out.println(ltApplicationContext.getBean("userService"));
        System.out.println(ltApplicationContext.getBean("orderService"));
        UserInterface userService = (UserInterface) ltApplicationContext.getBean("userService");
        userService.test();


    }
}
