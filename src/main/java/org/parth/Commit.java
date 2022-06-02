package org.parth;

public class Commit {
    private final String id;
    private final String name;
    private final String date;

    public Commit(String id,String name,String date)
    {
        this.id=id;
        this.name=name;
        this.date=date;
    }
    public void print()
    {
        System.out.println(id+" "+name+" "+date);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

}
