package com.lundincast.my_finance_manager.activities.model;

import java.util.Date;

/**
 * Created by lundincast on 28/02/15.
 */
public class Transaction{

    private long id;
    private double price;
    private String category;
    private Date date;
    private String comment;

    public Transaction(long id, double price, String category, Date date, String comment) {
        this.id = id;
        this.price = price;
        this.category = category;
        this.date = date;
        this.comment = comment;
    }

    public Transaction(short price, String category, Date date, String comment) {
        this.price = price;
        this.category = category;
        this.date = date;
        this.comment = comment;
    }

    public Transaction() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(short price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
