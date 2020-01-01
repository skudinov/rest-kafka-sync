
Kafka request-reply pattern implemented with Spring Boot.
Follows guidelines:
- [Using ReplyingKafkaTemplate](https://docs.spring.io/spring-kafka/reference/html/#replying-template)

At first, run kafka in docker: ```docker-compose up -d``` then follow ./rest-service/HELP.md

To see topic properties:
```
# docker ps 
# docker exec -it bb7fdd31e6a9 /bin/bash
# $KAFKA_HOME/bin/kafka-configs.sh --zookeeper zookeeper:2181 --describe --entity-name command.request --entity-type topics
```

Initial version derived from [Synchronous Request-Reply using Apache Kafka](https://github.com/callistaenterprise/blog-synchronous-kafka)
see https://callistaenterprise.se/blogg/teknik/2018/10/26/synchronous-request-reply-over-kafka/
