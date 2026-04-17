package org.ulpgc.dacd.control.persistence;

import com.google.gson.Gson;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.model.NewsArticle;

import jakarta.jms.*;
import java.util.List;

public class NewsPublisher implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson serializer;

    public NewsPublisher(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.serializer = EventSerializer.create();
    }

    @Override
    public void store(List<NewsArticle> articles) {
        try (Connection connection = createConnection()){


            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            for (NewsArticle article : articles) {
                String json = serializer.toJson(article);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
            }


        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection createConnection() throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
        Connection connection = factory.createConnection();
        connection.start();
        return connection;
    }
}