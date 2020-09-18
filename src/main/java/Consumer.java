import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import model.User;
import org.apache.commons.lang3.RandomStringUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

public class Consumer {

    private final static String EXCHANGE_NAME = "pdf";
    private final static String EXCHANGE_TYPE = "fanout";
    private final static String packageName = "pdf_package№" + RandomStringUtils.random(5, false, true);


    public static void main(String[] args) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        Scanner sc = new Scanner(System.in);
        System.out.println("Выберите тип файла");
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
                    //создание текстового документа
                    //кладем все данные туда, а потом конвертация в pdf
                    String helpFileName = RandomStringUtils.random(5, true, true) + ".txt";
                    PrintWriter pw = new PrintWriter(new FileWriter("created/txt/" + helpFileName));
                    pw.println(type);
                    pw.println(user.getName());
                    pw.println(user.getSurname());
                    pw.println(user.getAge());
                    pw.println(user.getNumber());
                    pw.println(user.getData());
                    pw.close();
                    //создание папки с pdf файлом
                    String folderName = "created/" + packageName;
                    String allFileName = folderName + "/" + RandomStringUtils.random(5, true, true) + ".pdf";
                    File folder = new File(folderName);
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    //создание pdf документа
                    Document document = new Document();
                    PdfWriter.getInstance(document, new FileOutputStream(allFileName));

                    document.open();
                    Font font = FontFactory.getFont(FontFactory.COURIER, 16, BaseColor.BLACK);

                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("created/txt/" + helpFileName)));
                    //запись в pdf текстовый файл
                    while (br.ready()) {
                        String str = br.readLine();
                        Chunk chunk = new Chunk(str, font);
                        document.add(chunk);
                    }
                    document.close();
                    br.close();
                    channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
                } catch (IOException | DocumentException e) {
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
