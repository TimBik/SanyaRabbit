import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import model.User;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private final static String EXCHANGE_NAME = "pdf";
    private final static String EXCHANGE_TYPE = "fanout";
    String packageName = "pdf_package№" + RandomStringUtils.random(5, false, true);


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Scanner sc = new Scanner(System.in);
        String type = sc.nextLine();

        try {
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicQos(3);

            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE);
            // создаем временную очередь со случайным названием
            String queue = channel.queueDeclare().getQueue();

            // привязали очередь к EXCHANGE_NAME
            channel.queueBind(queue, EXCHANGE_NAME, "");

            DeliverCallback deliverCallback = (consumerTag, message) -> {
                String jsonUser = new String(message.getBody());
                //json в юзера , заполнение pdf
                ObjectMapper mapper = new ObjectMapper();

                try {
                    User user = mapper.readValue(jsonUser, User.class);
                    String folderName = "created/" + packageName;
                    String allFileName = folderName + "/" + RandomStringUtils.random(5, true, true) + ".pdf";
                    File folder = new File(folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException e) {
                    System.err.println("FAILED");
                    channel.basicReject(message.getEnvelope().getDeliveryTag(), false);
                }
            };

            channel.basicConsume(queue, false, deliverCallback, consumerTag -> {});
        } catch (IOException | TimeoutException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
