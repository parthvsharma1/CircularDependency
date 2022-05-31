package org.sprinklrr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class twitter {
    private String name;
    private Integer number;

    @Autowired
    private facebook fb;

    twitter(){}
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public facebook getFb() {
        return fb;
    }

    public void setFb(facebook fb) {
        this.fb = fb;
    }
}
