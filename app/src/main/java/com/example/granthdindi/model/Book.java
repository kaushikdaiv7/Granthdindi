package com.example.granthdindi.model;

public class Book {
    private String englishName, marathiName, imgUrl, id;

    private int stocks, price;

    private int quantity;

    public Book(String englishName, String marathiName, String imgUrl, int stocks, int price, String id) {
        this.englishName = englishName;
        this.marathiName = marathiName;
        this.imgUrl = imgUrl;
        this.stocks = stocks;
        this.price = price;
        this.id = id;
    }

    public Book() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getEnglishName() {
        return englishName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getMarathiName() {
        return marathiName;
    }

    public void setMarathiName(String marathiName) {
        this.marathiName = marathiName;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public int getStocks() {
        return stocks;
    }

    public void setStocks(int stocks) {
        this.stocks = stocks;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
