package org.parth;

import org.springframework.stereotype.Component;

@Component
public class C {
    private String name;
    public C(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
