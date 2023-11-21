package com.lt.service;

import com.lt.spring.*;

@Componet("userService")
public class UserService implements BeanNameAware, InitializingBean,UserInterface {

    @Autowired
    public OrderService orderService ;

    public void test(){
        System.out.println(orderService);
    }

    private String beanName;
    @Override
    public void setBeanName(String beanName) {
        this.beanName=beanName;
    }

    @Override
    public void afterPropertiesSet() {

    }
}
