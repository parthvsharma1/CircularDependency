package org.parth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class A {
    private Integer number;
    @Autowired
    B b;

    public A(@Lazy B b) {
        super();
        this.b = b;
    }

    public B getB() {
        return b;
    }
    public void setB(B b) {
        this.b = b;
    }

    public void doSomeThing()
    {
        System.out.println("Doing some work");
    }
}