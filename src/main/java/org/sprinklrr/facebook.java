package org.sprinklrr;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class facebook {
    private List<Integer> list=new ArrayList<Integer>();
    private boolean b;
    facebook(){}

    public List<Integer> getList() {
        return list;
    }

    public void setList(List<Integer> list) {
        this.list = list;
    }

    public boolean isB() {
        return b;
    }

    public void setB(boolean b) {
        this.b = b;
    }
}
