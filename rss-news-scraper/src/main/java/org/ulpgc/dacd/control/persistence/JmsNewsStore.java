package org.ulpgc.dacd.control.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.ulpgc.dacd.model.NewsArticle;

import jakarta.jms.*;
import java.io.IOException;
import java.time.Instant;
import java.util.List;

public class JmsNewsStore implements NewsStore {
    private final String brokerUrl;
    private final String topicName;
    private final Gson gson;

    public JmsNewsStore(String brokerUrl, String topicName) {
        this.brokerUrl = brokerUrl;
        this.topicName = topicName;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
    }

    @Override
    public void store(List<NewsArticle> articles) {
        try {
            ConnectionFactory factory = new ActiveMQConnectionFactory(brokerUrl);
            Connection connection = factory.createConnection();
            connection.start();

            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topicName);
            MessageProducer producer = session.createProducer(destination);

            for (NewsArticle article : articles) {
                String json = gson.toJson(article);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
            }

            session.close();
            connection.close();
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private static class InstantAdapter extends TypeAdapter<Instant> {
        @Override
        public void write(JsonWriter out, Instant value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(value.toString());
            }
        }

        @Override
        public Instant read(JsonReader in) throws IOException {
            return Instant.parse(in.nextString());
        }
    }
}