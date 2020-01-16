package com.fri.code.exercises.services.streaming;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Response;

import com.kumuluz.ee.streaming.common.annotations.StreamProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.json.JSONObject;


@ApplicationScoped
public class EventProducerImpl {

    private static final String TOPIC_NAME = "4nqq16cf-grades";

    @Inject
    @StreamProducer
    private Producer producer;

    public Response produceExercises(Integer solvedExercises){

        ProducerRecord<String, String> record = new ProducerRecord<>(TOPIC_NAME, solvedExercises.toString());

        producer.send(record,
                (metadata, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                    }
//                    else {
//                        log.info("The offset of the produced message record is: " + metadata.offset());
//                    }
                });

        return Response.ok().build();

    }

}
