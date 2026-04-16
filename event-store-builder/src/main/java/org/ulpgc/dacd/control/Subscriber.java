package org.ulpgc.dacd.control;

import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;

public class Subscriber {

    private final String brokerUrl;
    private final String topicName;
    private final String clientId; // Durable
    private final EventStore store;

    public Subscriber(String brokerUrl, String topicName, String clientId, EventStore store) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.clientId = clientId;
        this.store = store;
    }

    public void start() {
        try {

            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection();

            connection.setClientID(clientId);
            connection.start();


            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = session.createTopic(topicName);

            MessageConsumer consumer = session.createDurableSubscriber(topic, clientId + "-sub");

            System.out.println("Suscrito a '" + topicName + "' de forma durable. Esperando eventos...");

            consumer.setMessageListener(message -> {
                try {
                    if (message instanceof TextMessage textMessage) {
                        String json = textMessage.getText();

                        store.save(topicName, json);
                    }
                } catch (JMSException e) {
                    System.err.println("Error al leer el mensaje: " + e.getMessage());
                }
            });

        } catch (JMSException e) {
            System.err.println("Error de conexión con ActiveMQ: " + e.getMessage());
        }
    }
}