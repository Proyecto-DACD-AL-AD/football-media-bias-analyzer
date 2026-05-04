package org.ulpgc.dacd.businessunit.control.subscriber;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.jms.*;
import java.util.function.BiConsumer;

public class Subscriber {

    private final Connection connection;
    private final String topicName;
    private final String subscriptionId;

    public Subscriber(Connection connection, String topicName, String subscriptionId) {
        this.connection = connection;
        this.topicName = topicName;
        this.subscriptionId = subscriptionId;
    }

    public void startConsuming(BiConsumer<String, JsonObject> eventConsumer) {
        try {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);
            MessageConsumer consumer = session.createDurableSubscriber(topic, subscriptionId);

            System.out.println("Suscrito a '" + topicName + "' de forma durable. Esperando eventos...");

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String json = textMessage.getText();
                        JsonObject event = JsonParser.parseString(json).getAsJsonObject();
                        eventConsumer.accept(topicName, event);
                    }
                } catch (JMSException e) {
                    System.err.println("Error al leer el mensaje: " + e.getMessage());
                }
            });

        } catch (JMSException e) {
            System.err.println("Error configurando el suscriptor para " + topicName + ": " + e.getMessage());
        }
    }
}