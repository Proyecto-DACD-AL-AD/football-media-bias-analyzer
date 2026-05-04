package org.ulpgc.dacd.api.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;
import org.ulpgc.dacd.api.model.Match;
import java.util.List;

public class FootballMatchPublisher implements FootballMatchStore {

    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;

    public FootballMatchPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
    }

    @Override
    public void store(List<Match> matches) {

        try (Connection connection = createConnection()){

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            for (Match match: matches) {
                String jsonEvent = serializer.toJson(match);
                TextMessage message = session.createTextMessage(jsonEvent);
                producer.send(message);
            }

            System.out.println("Se han enviado todos los mensajes al topic: '" + topicName + "'...");

        } catch (JMSException e) {

            System.err.println("Error al enviar el mensaje a ActiveMQ: " + e.getMessage());
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}