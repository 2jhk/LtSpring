package com.lt.service;

import com.lt.spring.BeanPostProcessor;
import com.lt.spring.Componet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Componet
public class LtBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object PostProcessBeforeInitialization(String beanName, Object bean) {
        if(beanName.equals("userService")){
            System.out.println("前置处理器处理"+beanName);
        }
        return bean;
    }

    @Override
    public Object PostProcessAfterInitialization(String beanName, Object bean) {
        // 判断beanName是否为"orderService"
        if(beanName.equals("orderService")){
            // 输出后置处理器处理orderService
            System.out.println("后置处理器处理" + beanName);
        }
        // 判断beanName是否为"userService"
        if(beanName.equals("userService")){
            // 创建一个新的代理实例
            Object newProxyInstance = Proxy.newProxyInstance(LtBeanPostProcessor.class.getClassLoader(),
                    bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    // 执行切面逻辑
                    System.out.println("切面逻辑");
                    System.out.println(method);
                    // 调用方法
                    return method.invoke(bean,args);
                }
            });
            // 返回新的代理实例
            return newProxyInstance;
        }
        // 若不是特定bean，则返回原bean
        return bean;
    }

}
