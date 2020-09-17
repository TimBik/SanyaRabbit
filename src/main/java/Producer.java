import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import model.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Producer {
    // есть EXCHANGE - images НЕ ОЧЕРЕДЬ
    private final static String EXCHANGE_NAME = "pdf";
    // тип FANOUT
    private final static String EXCHANGE_TYPE = "fanout";
    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Scanner sc = new Scanner(System.in);
        System.out.println("Введите имя");
        String name = sc.nextLine();
        System.out.println("Ввыедите фамилию");
        String surname = sc.nextLine();
        System.out.println("Введите номер паспорта");
        String number = sc.nextLine();
        System.out.println("Введите возраст");
        String age = sc.nextLine();
        System.out.println("Введите дату выдачи");
        String data = sc.nextLine();

        User user = new User(name,surname,number,Integer.parseInt(age), data);
        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // создаем exchange
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            ObjectMapper mapper = new ObjectMapper();
            String jsonObject = mapper.writeValueAsString(user);
            //считывание с клавиатуры создание юзера юзера превращаем в json
            // считываем файл URL
            channel.basicPublish(EXCHANGE_NAME, "",null, jsonObject.getBytes());
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }

    }
}
