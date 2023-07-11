package com.booking.processing;

import com.booking.room.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RoomMessageConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(RoomMessageConsumer.class);

    @KafkaListener(
            topics = "${spring.kafka.topic.name}"
            ,group = "${spring.kafka.consumer.group-id}"
    )
    public void consume(Room event){
        LOGGER.info(String.format("Room event received in email service => %s", event.toString()));

        // send an email to the customer
        //we will send a notification that a new room has been reserved

    }
}