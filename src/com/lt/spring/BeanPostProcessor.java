package com.lt.spring;

public interface BeanPostProcessor {

    public Object PostProcessBeforeInitialization(String beanName,Object bean);
    public Object PostProcessAfterInitialization(String beanName,Object bean);
}
