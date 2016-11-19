package com.github.ftoresan.currency;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
public class Conversion {

    private String fromCountry;
    private String toCountry;
    private double value;

    public Conversion() {}

    public Conversion(String fromCountry, String toCountry, double value) {
        this.fromCountry = fromCountry;
        this.toCountry = toCountry;
        this.value = value;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public String getToCountry() {
        return toCountry;
    }

    public double getValue() {
        return value;
    }
}
