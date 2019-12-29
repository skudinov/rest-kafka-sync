package samplr.restservice;

import com.epam.gdansk.model.Command;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class RestHandler {
  @Autowired
  private ReplyingKafkaTemplate<String, String, Command> kafkaTemplate;

  @Value("${kafka.topic.command.request}")
  private String requestTopic;

  public Mono<ServerResponse> create(ServerRequest request) {
    ProducerRecord<String, String> record = new ProducerRecord<>(requestTopic, "1", "Hello");
    RequestReplyFuture<String, String, Command> reply = kafkaTemplate.sendAndReceive(record);
    return Mono.fromFuture(reply.completable())
        .flatMap(r -> ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(r.value()));
  }
}
