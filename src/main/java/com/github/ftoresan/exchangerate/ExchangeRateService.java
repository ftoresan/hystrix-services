package com.github.ftoresan.exchangerate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Fabricio Toresan on 05/11/16.
 */
@EnableAutoConfiguration
@Controller
public class ExchangeRateService {

    private Map<String, Double> exchangeRates = new HashMap<>();

    public ExchangeRateService() {
        exchangeRates.put("BRL@USD", 0.31);
        exchangeRates.put("USD@BRL", 3.22);
        exchangeRates.put("BRL@EUR", 0.28);
        exchangeRates.put("EUR@BRL", 3.51);
        exchangeRates.put("BRL@MXN", 6.15);
        exchangeRates.put("MXN@BRL", 0.16);

    }

    @RequestMapping(value = "/exchange", method = RequestMethod.GET)
    @ResponseBody
    public double calculateExchange(@RequestParam("from") String from, @RequestParam("to") String to, @RequestParam("value") double value) {
        double d = exchangeRates.get(from + "@" + to);
        d = d * (1 + (new Random().nextInt(10) / 100d));
        return d;
    }

    public static void main(String[] args) {
        SpringApplication.run(ExchangeRateService.class, args);
    }
}
