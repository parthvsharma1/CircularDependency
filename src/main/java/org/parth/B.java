package org.parth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class B {

    @Autowired
    A a;

    @Autowired
//    @Qualifier("c1")// if we have more than 1 bean then srping cannot match by type
//                    // if name is also not matching with any bean then we will have to explicitly define which beanid
//                    // we want by the qualifier
    C c;

    public C getC() {
        return c;
    }


    public void setC(C c) {
        this.c = c;
    }

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