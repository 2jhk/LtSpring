package com.lt.spring;

import java.beans.Introspector;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class LtApplicationContext {
    private Class configClass;
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    private ArrayList<BeanPostProcessor>beanPostProcessorList=new ArrayList<>();

    public LtApplicationContext(Class configClass) {
        this.configClass = configClass;
        //扫描----->BeanDefinition--->beanDefinitionMap    本质上是扫描对应.class文件
        if (configClass.isAnnotationPresent(ComponetScan.class)) {
            ComponetScan annotation = (ComponetScan) configClass.getAnnotation(ComponetScan.class);
            String path = annotation.value();//com.lt.service
            path = path.replace(".", "/");
            //类加载器根据当前路径获取绝对路径
            ClassLoader classLoader = LtApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);
            File file = new File(resource.getFile());
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if (fileName.endsWith(".class")) {
                        //取巧方式获取类名  实际上不能采用这种方式，后续要修改
                        //todo 获取类名
                        String className = fileName.substring(fileName.indexOf("com"), fileName.indexOf(".class"));
                        //com\lt\service\UserService
                        className = className.replace("\\", ".");

                        try {
                            Class<?> aClass = classLoader.loadClass(className);
                            //判断是不是一个bean
                            if (aClass.isAnnotationPresent(Componet.class)) {
                                //判断是不是beanPostProcessor
                                //判断类是不是由接口派生
                                if(BeanPostProcessor.class.isAssignableFrom(aClass)){
                                    BeanPostProcessor instance = (BeanPostProcessor) aClass.newInstance();
                                    beanPostProcessorList.add(instance);
                                }

                                Componet componet = aClass.getAnnotation(Componet.class);
                                String beanName = componet.value();
                                if (beanName.equals("")) {
                                    //spring根据类名生成对应beanName  首字母小写
                                    beanName = Introspector.decapitalize(aClass.getSimpleName());
                                }
                                //创建beanDefinition对象
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(aClass);
                                if (aClass.isAnnotationPresent(Scope.class)) {
                                    //获取Scope并赋值
                                    Scope scopeAnnotation = aClass.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());
                                } else {
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);

                            }
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        } catch (InstantiationException e) {
                            throw new RuntimeException(e);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            //实例化单例bean
            for (String beanName : beanDefinitionMap.keySet()) {
                BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
                if (beanDefinition.getScope().equals("singleton")) {
                    Object bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
            }
        }
    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class type = beanDefinition.getType();
        try {
            //bean的生命周期
            //1.实例化bean对象
            Object instance = type.getConstructor().newInstance();
            //2.依赖注入
            for (Field f : type.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    //必须首先设置accessible才能对私有属性赋值
                    f.setAccessible(true);
                    f.set(instance, getBean(f.getName()));
                }
            }
            //3.Aware回调
            if (instance instanceof BeanNameAware) {
                ((BeanNameAware) instance).setBeanName(beanName);
            }
            //前置处理器
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
              instance=  beanPostProcessor.PostProcessBeforeInitialization(beanName,instance);
            }
            //4.初始化
            if (instance instanceof InitializingBean) {
                ((InitializingBean) instance).afterPropertiesSet();
            }
            //后置处理器
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                //初始化后AOP
                instance=  beanPostProcessor.PostProcessAfterInitialization(beanName,instance);
            }


            return instance;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            throw new NullPointerException();
        } else {
            String scope = beanDefinition.getScope();
            if ("singleton".equals(scope)) {
                //单例
                Object bean = singletonObjects.get(beanName);
                if (bean == null) {
                    Object o = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, o);
                }
                return bean;
            } else {
                //多例
                return createBean(beanName, beanDefinition);
            }
        }
    }
}
