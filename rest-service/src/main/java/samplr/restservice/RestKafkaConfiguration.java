package samplr.restservice;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import samplr.model.Command;

@Configuration
@EnableKafka
public class RestKafkaConfiguration {

  @Value("${kafka.topic.command.request}")
  private String requestTopicName;

  @Value("${kafka.topic.command.reply}")
  private String replyTopicName;

  @Value("${kafka.request-reply.timeout-ms}")
  private Long replyTimeout;

  @Value("${spring.kafka.consumer.group-id}")
  private String replyConsumerGroupId;

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  public ReplyingKafkaTemplate<String, String, Command> replyKafkaTemplate(
      ProducerFactory<String, String> requestProducerFactory,
      ConcurrentMessageListenerContainer<String, Command> replyContainer) {
    return new ReplyingKafkaTemplate<>(requestProducerFactory, replyContainer);
  }

  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  @Bean
  public ConcurrentMessageListenerContainer<String, Command> replyContainer(
      ConcurrentKafkaListenerContainerFactory<String, Command> containerFactory) {

    ConcurrentMessageListenerContainer<String, Command> repliesContainer = containerFactory.createContainer(replyTopicName);
    repliesContainer.getContainerProperties().setGroupId(replyConsumerGroupId);
    repliesContainer.setAutoStartup(false);
    return repliesContainer;
  }

  @Bean
  public NewTopic requestTopic() {
    return TopicBuilder.name(requestTopicName)
        .partitions(2)
        .replicas(1)
//        .config(TopicConfig.RETENTION_MS_CONFIG, replyTimeout.toString())
        .build();
  }

  @Bean
  public NewTopic replyTopic() {
    return TopicBuilder.name(replyTopicName)
        .partitions(2)
        .replicas(1)
//        .config(TopicConfig.RETENTION_MS_CONFIG, replyTimeout.toString())
        .build();
  }

}
