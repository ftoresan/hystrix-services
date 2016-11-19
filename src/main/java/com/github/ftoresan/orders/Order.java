package com.github.ftoresan.orders;

import java.time.LocalDate;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
public class Order {

    private int number;
    private LocalDate creationDate;
    private String customer;
    private String country;
    private double totalValue;

    public Order(int number, LocalDate creationDate, String customer, String country, double totalValue) {
        this.number = number;
        this.creationDate = creationDate;
        this.customer = customer;
        this.country = country;
        this.totalValue = totalValue;
    }

    public int getNumber() {
        return number;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public String getCustomer() {
        return customer;
    }

    public String getCountry() {
        return country;
    }

    public double getTotalValue() {
        return totalValue;
    }

    @Override
    public String toString() {
        return "Order{" +
                "number=" + number +
                ", creationDate=" + creationDate +
                ", customer='" + customer + '\'' +
                ", country='" + country + '\'' +
                ", totalValue=R$ " + totalValue +
                '}';
    }
}
