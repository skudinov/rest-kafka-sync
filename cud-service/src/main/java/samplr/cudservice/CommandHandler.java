package samplr.cudservice;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;
import samplr.model.Command;

import java.util.UUID;

@Component
public class CommandHandler {

  @KafkaListener(id="cud-command", topics = "${kafka.topic.command.request}")
  @SendTo()
  public Command receive(String operation) {
    return new Command(UUID.randomUUID().toString(), operation);
  }
}
