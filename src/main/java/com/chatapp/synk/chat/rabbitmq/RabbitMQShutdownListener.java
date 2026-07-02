package com.chatapp.synk.chat.rabbitmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

@Component
public class RabbitMQShutdownListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RabbitMQShutdownListener.class);

    private final RabbitAdmin rabbitAdmin;
    private final Queue myQueue; // The queue bean you want to delete
    private final Binding myBinding; // The binding bean you want to delete

    /**
     * Constructor to inject the necessary beans.
     * @param rabbitAdmin The admin utility to interact with the RabbitMQ broker.
     * @param myQueue The specific queue bean to be managed.
     * @param myBinding The specific binding bean to be managed.
     */
    public RabbitMQShutdownListener(RabbitAdmin rabbitAdmin, Queue myQueue, Binding myBinding) {
        this.rabbitAdmin = rabbitAdmin;
        this.myQueue = myQueue;
        this.myBinding = myBinding;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        logger.info("Application is shutting down. Cleaning up RabbitMQ resources...");

        // 1. Remove the binding
        // This stops new messages from being routed to the queue via this binding.
        logger.info("Removing binding for queue: {}", myQueue.getName());
        rabbitAdmin.removeBinding(myBinding);

        // 2. Delete the queue
        // The 'true' argument checks if the queue is unused before deleting.
        // The 'true' argument checks if the queue is empty before deleting.
        // Use deleteQueue(queueName, false, false) to force delete.
        logger.info("Deleting queue: {}", myQueue.getName());
        boolean deleted = rabbitAdmin.deleteQueue(myQueue.getName());
        if (deleted) {
            logger.info("Successfully deleted queue '{}'.", myQueue.getName());
        } else {
            logger.warn("Could not delete queue '{}'. It might not be empty or is in use.", myQueue.getName());
        }
    }
}