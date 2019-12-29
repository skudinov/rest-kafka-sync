package samplr.cudservice;

import com.epam.gdansk.model.Command;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommandHandler {

  @KafkaListener(topics = "${kafka.topic.command.request}", containerFactory = "requestReplyListenerContainerFactory")
  @SendTo()
  public Command receive(String operation) {
    return new Command(UUID.randomUUID().toString(), operation);
  }
}