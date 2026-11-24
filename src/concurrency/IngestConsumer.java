package concurrency;

import exception.DuplicateRecordException;
import exception.InvalidEntityException;
import model.Invoice;
import service.EntityService;

public class IngestConsumer implements Runnable {
    private final IngestQueue queue;
    private final EntityService entityService;

    public IngestConsumer(IngestQueue queue, EntityService entityService) {
        this.queue = queue;
        this.entityService = entityService;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        try {
            Invoice invoice;
            while ((invoice = queue.take()) != null) {
                String invoiceId = invoice.getId();
                try {
                    entityService.registerEntity(invoice);
                    System.out.println("[" + threadName + "] Registered invoice: " + invoiceId);
                } catch (InvalidEntityException | DuplicateRecordException e) {
                    System.out.println("[" + threadName + "] Rejected invoice " + invoiceId + ": " + e.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[" + threadName + "] Consumer interrupted.");
        }
    }
}