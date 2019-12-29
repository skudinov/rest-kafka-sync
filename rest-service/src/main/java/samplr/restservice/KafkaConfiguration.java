package samplr.restservice;

import com.epam.gdansk.model.Command;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaConfiguration {
  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.topic.command.request}")
  private String requestTopic;

  @Value("${kafka.topic.command.reply}")
  private String replyTopic;

  @Value("${kafka.request-reply.timeout-ms}")
  private Long replyTimeout;

  @Value("${spring.kafka.consumer.group-id}")
  private String groupId;

  @Bean
  public ReplyingKafkaTemplate<String, String, Command> replyKafkaPattern() {
    return new ReplyingKafkaTemplate<>(requestProducerFactory(), replyListenerContainer());
  }

  public ProducerFactory<String, String> requestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  public Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  public Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

    return props;
  }

  public ConsumerFactory<String, Command> replyConsumerFactory() {
    JsonDeserializer<Command> jsonDeserializer = new JsonDeserializer<Command>();
    jsonDeserializer.addTrustedPackages(Command.class.getPackage().getName());
    return new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(),
        jsonDeserializer);
  }

  public KafkaMessageListenerContainer<String, Command> replyListenerContainer() {
    ContainerProperties containerProperties = new ContainerProperties(replyTopic);
    return new KafkaMessageListenerContainer<>(replyConsumerFactory(), containerProperties);
  }

}
