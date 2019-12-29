package samplr.restservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import samplr.model.Command;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class RestKafkaConfiguration {
  @Value("${spring.kafka.bootstrap-servers}")
  private String bootstrapServers;

  @Value("${kafka.topic.command.request}")
  private String requestTopicName;

  @Value("${kafka.topic.command.reply}")
  private String replyTopicName;

  @Value("${kafka.request-reply.timeout-ms}")
  private Long replyTimeout;

  @Value("${spring.kafka.consumer.group-id}")
  private String consumerGroupId;

  @Bean
  public ReplyingKafkaTemplate<String, String, Command> replyKafkaTemplate(
      ProducerFactory<String, String> requestProducerFactory,
      ConcurrentMessageListenerContainer<String, Command> replyContainer) {
    return new ReplyingKafkaTemplate<>(requestProducerFactory, replyContainer);
  }

  @Bean
  public ProducerFactory<String, String> requestProducerFactory() {
    return new DefaultKafkaProducerFactory<>(producerConfigs());
  }

  private Map<String, Object> producerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    return props;
  }

  private Map<String, Object> consumerConfigs() {
    Map<String, Object> props = new HashMap<>();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);

    return props;
  }

  @Bean
  // replaces bean in KafkaAutoConfiguration
  public ConsumerFactory<Object, Object> kafkaConsumerFactory() {
    JsonDeserializer<Command> jsonDeserializer = new JsonDeserializer<Command>();
    jsonDeserializer.addTrustedPackages(Command.class.getPackage().getName());
    return (ConsumerFactory) new DefaultKafkaConsumerFactory<>(consumerConfigs(), new StringDeserializer(), jsonDeserializer);
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  public ConcurrentMessageListenerContainer<String, Command> replyContainer(
      ConcurrentKafkaListenerContainerFactory<String, Command> containerFactory) {

    ConcurrentMessageListenerContainer<String, Command> repliesContainer = containerFactory.createContainer(replyTopicName);
    repliesContainer.getContainerProperties().setGroupId(consumerGroupId);
    repliesContainer.setAutoStartup(false);
    return repliesContainer;
  }

//  @Bean
//  public KafkaMessageListenerContainer<String, Command> replyListenerContainer(ConsumerFactory<String, Command> replyConsumerFactory) {
//    ContainerProperties containerProperties = new ContainerProperties(replyTopicName);
//    return new KafkaMessageListenerContainer<>(replyConsumerFactory, containerProperties);
//  }

  @Bean
  public NewTopic replyTopic() {
    return TopicBuilder.name(replyTopicName)
        .partitions(2)
        .replicas(2)
        .build();
  }
}
