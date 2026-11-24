package concurrency;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import java.util.concurrent.BlockingQueue;
import model.DetectionReport;
import model.Invoice;
import service.DetectionService;
import service.EntityService;

public class AuditConsumer implements Runnable {
    private final BlockingQueue<Invoice> queue;
    private final EntityService entityService;
    private final DetectionService detectionService;
    private int processedCount;

    public AuditConsumer(
            BlockingQueue<Invoice> queue,
            EntityService entityService,
            DetectionService detectionService) {
        this.queue = queue;
        this.entityService = entityService;
        this.detectionService = detectionService;
        this.processedCount = 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                Invoice invoice = queue.take();

                if (invoice == IngestProducer.POISON_PILL || "INV-000".equals(invoice.getId())) {
                    System.out.println("[Consumer] Stop signal received. Done.");
                    break;
                }

                try {
                    entityService.register(invoice);
                    processedCount++;
                    DetectionReport report = detectionService.runAll(entityService.getAll());
                    System.out.println(
                            "[Consumer] Saved " + invoice.getId()
                                    + " | New fraud links found: " + report.getNewLinks().size());
                } catch (DuplicateRecordException | InvalidEntityException e) {
                    System.out.println("[Consumer] Skipped " + invoice.getId() + " (" + e.getMessage() + ")");
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public int getProcessedCount() {
        return processedCount;
    }
}