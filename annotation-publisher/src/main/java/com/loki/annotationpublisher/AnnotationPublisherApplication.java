package com.loki.annotationpublisher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AnnotationPublisherApplication {

    private static final Logger logger =
            LogManager.getLogger(AnnotationPublisherApplication.class);

    public static void main(String[] args) {
        logger.info("Starting Grafana Annotation Publisher");
        SpringApplication.run(AnnotationPublisherApplication.class, args);
        logger.info("Grafana Annotation Publisher started successfully");
    }
}