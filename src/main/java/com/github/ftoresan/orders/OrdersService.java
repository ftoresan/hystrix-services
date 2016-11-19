package com.github.ftoresan.orders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ftoresan.currency.Conversion;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
@Component
public class OrdersService {

    private List<Order> orders = new ArrayList<>();
    private BlockingQueue queue = new SynchronousQueue();
    private RabbitTemplate rabbit;

    @Autowired
    public OrdersService(RabbitTemplate rabbit, AmqpAdmin amqpAdmin, Queue orderQueue, TopicExchange topic) {
        this.rabbit = rabbit;

        Binding binding = BindingBuilder.bind(orderQueue).to(topic).with("orders");
        amqpAdmin.declareBinding(binding);

        orders.add(new Order(1, LocalDate.of(2016, 11, 2), "Bill Carson", "USA", 1500));
        orders.add(new Order(2, LocalDate.of(2016, 10, 31), "Pablo Ramirez", "Mexico", 550.0));
        orders.add(new Order(3, LocalDate.of(2016, 11, 1), "Alberto Grimaldi", "Italy", 440));
        orders.add(new Order(4, LocalDate.of(2016, 10, 15), "Eugenio Alabiso", "Brazil", 2500));
        orders.add(new Order(5, LocalDate.of(2016, 11, 2), "Mickey Knox", "USA", 890));
    }

    @RabbitListener(queues = {"orders"})
    public void processOrderCommand(MessageHeaders headers, Message message, String data) {
        try {
            if (!headers.containsKey("command")) {
                System.err.println("No command");
                return;
            }
            String command = headers.get("command").toString();
            if (command.equals("listOrders")) {
                listOrders();
            } else if (command.equals("getOrderValue")) {
                int orderNo = Integer.parseInt(data);
                getOrderValue(orderNo);
            } else if (command.equals("currencyCalculated")) {
                queue.put(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void listOrders() {
        orders.forEach(o -> System.out.println(o));
    }

    private void getOrderValue(int orderNo) throws InterruptedException, ExecutionException, TimeoutException {
        Order order = orders.get(orderNo - 1);
        ConvertCommand command = new ConvertCommand(HystrixCommandGroupKey.Factory.asKey("OrderCurrency"), order);
        Future result = command.queue();
        String finalValue = (String) result.get(2, TimeUnit.SECONDS);
        if (finalValue.equals("$ERROR$")) {
            printOriginalValue(order);
        } else {
            System.out.println("The converted order value is " + finalValue);
        }
    }

    private void printOriginalValue(Order order) {
        System.out.println("Could not convert the value. The value in Real is R$ " + order.getTotalValue());
    }

    public class ConvertCommand extends HystrixCommand {

        private final Order order;

        protected ConvertCommand(HystrixCommandGroupKey group, Order order) {
            super(group);
            this.order = order;
        }

        @Override
        protected Object run() throws Exception {
            Conversion conversion = new Conversion("Brazil", order.getCountry(), order.getTotalValue());
            ObjectMapper mapper = new ObjectMapper();
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setHeader("routingToResponse", "orders");
            Message message = new Message(mapper.writeValueAsBytes(conversion), properties);
            rabbit.send("amq.topic", "currency", message);
            Object result =  queue.poll(1, TimeUnit.SECONDS); // We need to wait less then the command timeout
            if (result == null) {
                throw new RuntimeException("Result not available");
            }
            return result;
        }

        @Override
        protected Object getFallback() {
            return "$ERROR$";
        }
    }
}
