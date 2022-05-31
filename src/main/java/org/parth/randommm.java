package org.parth;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.lang.reflect.InvocationTargetException;


public class randommm {
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        ClassPathXmlApplicationContext ac = new ClassPathXmlApplicationContext("ApplicationContext.xml");
        A a = (A) ac.getBean("a");
        a.doSomeThing();
        ac.close();

        System.out.println(a.toString());

//        String className="org.parth.sample";
//        Class<?> cls = Class.forName(className);
//
//        //create instance of that class
//
//        Object myInstance = cls.getDeclaredConstructor().newInstance();
//        System.out.println(myInstance.toString());
    }
}
