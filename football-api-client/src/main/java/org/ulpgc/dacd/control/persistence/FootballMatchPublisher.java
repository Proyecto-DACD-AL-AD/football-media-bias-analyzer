package org.ulpgc.dacd.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import jakarta.jms.*;
import org.ulpgc.dacd.model.Match;
import java.util.List;

public class FootballMatchPublisher implements FootballMatchStore {

    private final String brokerUrl;
    private final String topicName;
    private final Gson gson;

    public FootballMatchPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.gson = EventSerializer.create();
    }

    public void store(List<Match> matches) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            for (Match match: matches) {
                String jsonEvent = gson.toJson(match);
                TextMessage message = session.createTextMessage(jsonEvent);
                producer.send(message);
            }

            System.out.println("Se han enviado todos los mensajes al topic: '" + topicName + "'...");
            connection.close();

        } catch (JMSException e) {
            System.err.println("Error al enviar el mensaje a ActiveMQ: " + e.getMessage());
        }
    }
}
