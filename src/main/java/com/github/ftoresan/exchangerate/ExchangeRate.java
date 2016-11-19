package com.github.ftoresan.exchangerate;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by Fabricio Toresan on 09/11/16.
 */
public class ExchangeRate {

    private double exchangeValue;
    private String symbol;

    public ExchangeRate() {
    }

    public ExchangeRate(double exchangeValue, String symbol) {
        this.exchangeValue = exchangeValue;
        this.symbol = symbol;
    }

    public double getExchangeValue() {
        return exchangeValue;
    }

    public void setExchangeValue(double exchangeValue) {
        this.exchangeValue = exchangeValue;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        NumberFormat df = DecimalFormat.getInstance();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        return symbol + " " + df.format(exchangeValue);
    }
}
