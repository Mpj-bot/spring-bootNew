package com.example.springdemo;

import java.util.Objects;

public class demospring {
    private int id;
    private String name;
    private String cose;

    public demospring() {
    }

    public demospring(int id,
                      String name,
                      String cose) {
        this.id = id;
        this.name = name;
        this.cose = cose;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCose() {
        return cose;
    }

    public void setCose(String cose) {
        this.cose = cose;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        demospring that = (demospring) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(cose, that.cose);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, cose);
    }
}
