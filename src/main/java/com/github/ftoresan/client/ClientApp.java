package com.github.ftoresan.client;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitManagementTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Fabricio Toresan on 02/11/16.
 */
public class ClientApp {

    private static ApplicationContext context;
    private static BufferedReader input;

    public static void main(String[] args) throws IOException {
        context = new AnnotationConfigApplicationContext(ClientConfiguration.class);

        input = new BufferedReader(new InputStreamReader(System.in));
        boolean exit = false;
        while (!exit) {
            printMenu();
            String inputValue = input.readLine();
            int option = Integer.parseInt(inputValue);
            switch (option) {
                case 1:
                    sendListOrders();
                    break;
                case 2:
                    requestOrderValue();
                    break;
                case 3:
                    exit = true;
                    break;
            }
        }
        CachingConnectionFactory connFactory = context.getBean(CachingConnectionFactory.class);
        connFactory.destroy();

    }

    private static void requestOrderValue() throws IOException {
        System.out.println("\nEnter order number");
        System.out.print("> ");
        String inputValue = input.readLine();

        RabbitTemplate rabbit = context.getBean(RabbitTemplate.class);
        MessageProperties properties = MessagePropertiesBuilder.newInstance().setHeader("command", "getOrderValue").build();
        properties.setContentType("text/plain");
        Message message = new Message(inputValue.getBytes(), properties);
        rabbit.send("amq.topic", "orders", message);
    }

    private static void sendListOrders() {
        RabbitTemplate rabbit = context.getBean(RabbitTemplate.class);
        MessageProperties properties = MessagePropertiesBuilder.newInstance().setHeader("command", "listOrders").build();
        Message message = new Message("{}".getBytes(), properties);
        rabbit.send("amq.topic", "orders", message);
    }

    private static void printMenu() {
        System.out.println("Choose an option:");
        System.out.println("[1] List all orders");
        System.out.println("[2] Show an order value");
        System.out.println("[3] Exit");
        System.out.print("> ");
    }
}
