package com.github.ftoresan.currency;

import com.github.ftoresan.exchangerate.ExchangeRate;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.AbstractClientHttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
@Component
public class CurrencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyService.class);

    private final Map<String, String> currencySymbols = new HashMap<>();
    private Map<String, String> currencies = new HashMap<>();
    private Map<String, Double> exchangeCache = new HashMap<>();

    private RabbitTemplate rabbit;

    @Autowired
    public CurrencyService(RabbitTemplate rabbit, AmqpAdmin amqpAdmin, Queue currencyQueue, TopicExchange topic) {
        Binding binding = BindingBuilder.bind(currencyQueue).to(topic).with("currency");
        amqpAdmin.declareBinding(binding);

        this.rabbit = rabbit;
        currencies.put("Brazil", "BRL");
        currencies.put("Mexico", "MXN");
        currencies.put("USA", "USD");
        currencies.put("Italy", "EUR");

        currencySymbols.put("BRL", "R$");
        currencySymbols.put("USD", "US$");
        currencySymbols.put("EUR", "€");
        currencySymbols.put("MXN", "$");

    }

    @RabbitListener(queues = {"currency"})
    public void processCurrencyCommand(MessageHeaders headers, @Payload Conversion conversion) {
        if (!currencies.containsKey(conversion.getFromCountry()) || !currencies.containsKey(conversion.getToCountry())) {
            System.err.println("Country not supported");
            return;
        }
        try {
            String from = currencies.get(conversion.getFromCountry());
            String to = currencies.get(conversion.getToCountry());
            convert(from, to, conversion.getValue(), headers.get("routingToResponse").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convert(String from, String to, double value, String sendTo) throws InterruptedException, ExecutionException, TimeoutException {
        MessageProperties properties = MessagePropertiesBuilder.newInstance().build();
        properties.setContentType("text/plain");
        properties.setHeader("command", "currencyCalculated");

        Double exchangeRate = new Double(1d);
        boolean error = false;
        if (!from.equals(to)) {
            ExchangeRateCommand exchangeCommand = new ExchangeRateCommand(HystrixCommandGroupKey.Factory.asKey("exchangeRate"), from, to, value);
            exchangeRate = (Double) exchangeCommand.queue().get(2, TimeUnit.SECONDS);
            if (exchangeRate > -1) { // don't cache errors
                exchangeCache.put(from + "@" + to, exchangeRate);
            } else {
                error = true;
            }
        }
        String result;
        if (error) {
            result = "$ERROR$";
        } else {
            double d = value * exchangeRate;
            result = currencySymbols.get(to) + " " + new BigDecimal(d).setScale(2, BigDecimal.ROUND_HALF_UP).toString();
        }
        Message message = new Message(result.getBytes(), properties);
        rabbit.send("amq.topic", sendTo, message);
    }

    public class ExchangeRateCommand extends HystrixCommand {

        private final String from;
        private final String to;
        private final double value;

        protected ExchangeRateCommand(HystrixCommandGroupKey group, String from, String to, double value) {
            super(group);
            this.from = from;
            this.to = to;
            this.value = value;
        }

        @Override
        protected Object run() throws Exception {
            RestTemplate restTemplate = new RestTemplate();
            StringBuilder sb = new StringBuilder("http://localhost:8080/exchange?");
            sb.append("from=").append(from);
            sb.append("&to=").append(to);
            sb.append("&value=").append(value);
            Double exchangeRate = restTemplate.getForObject(sb.toString(), Double.class);
            return exchangeRate;
        }

        @Override
        protected Object getFallback() {
            Double cachedRate = exchangeCache.get(from + "@" + to);
            if (cachedRate != null) {
                LOGGER.warn("Warning: using cached value");
                return cachedRate;
            }
            return new Double(-1d);
        }
    }

}
