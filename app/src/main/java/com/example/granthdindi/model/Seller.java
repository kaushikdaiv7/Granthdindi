package com.example.granthdindi.model;

public class Seller {

    private String name;
    private Long contact;
    private String location;
    private long cash;

    public Seller() {}

    public Seller(String name, Long contact, String location, int cash) {
        this.name = name;
        this.contact = contact;
        this.location = location;
        this.cash = cash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getContact() {
        return contact;
    }

    public void setContact(Long contact) {
        this.contact = contact;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getCash() {
        return cash;
    }

    public void setCash(int cash) {
        this.cash = cash;
    }
}
