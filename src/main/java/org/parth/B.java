package org.parth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class B {
    A a;
    @Autowired
    public B(A a) {
        super();
        this.a = a;
    }

    public A getA() {
        return a;
    }

    public void setA(A a) {
        this.a = a;
    }
}
