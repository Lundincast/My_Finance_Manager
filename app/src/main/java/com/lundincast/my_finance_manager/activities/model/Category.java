package com.lundincast.my_finance_manager.activities.model;

/**
 * Created by lundincast on 21/02/15.
 */
public class Category {

    private long id;
    private String name;
    private String color;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return name;
    }
}
