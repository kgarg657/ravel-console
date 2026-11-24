package concurrency;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import model.Invoice;
import service.DetectionService;
import service.EntityService;

public class QueueDemo {
    private final EntityService entityService;
    private final DetectionService detectionService;

    public QueueDemo(EntityService entityService, DetectionService detectionService) {
        this.entityService = entityService;
        this.detectionService = detectionService;
    }

    public void run() {
        BlockingQueue<Invoice> queue = new ArrayBlockingQueue<>(3);

        IngestProducer producer = new IngestProducer(queue);
        AuditConsumer consumer = new AuditConsumer(queue, entityService, detectionService);

        Thread producerThread = new Thread(producer, "Invoice-Producer");
        Thread consumerThread = new Thread(consumer, "Audit-Consumer");

        System.out.println("\n=== Live Invoice Ingestion Queue Demo ===");
        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("--- Queue Demo Complete ---");
        System.out.println("New invoices saved : " + consumer.getProcessedCount());
        System.out.println("Total entities now : " + entityService.getAll().size());
    }
}