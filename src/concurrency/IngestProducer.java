package concurrency;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import model.Invoice;

public class IngestProducer implements Runnable {
    public static final Invoice POISON_PILL = new Invoice(
            "INV-000", "POISON_PILL", 0, 1.0, "EMP-000", "VEN-000", LocalDate.of(2026, 1, 1));

    private final BlockingQueue<Invoice> queue;

    public IngestProducer(BlockingQueue<Invoice> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        List<Invoice> batch = buildDemoInvoices();
        try {
            for (Invoice invoice : batch) {
                queue.put(invoice);
                System.out.println("[Producer] Queued " + invoice.getId() + " (" + invoice.getName() + ")");
                Thread.sleep(200);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try {
                queue.put(POISON_PILL);
                System.out.println("[Producer] Finished stream and sent stop signal.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<Invoice> buildDemoInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        invoices.add(new Invoice(
                "INV-103", "Cloud Hosting Fee", 0, 75000.0,
                "EMP-001", "VEN-001", LocalDate.of(2026, 11, 10)));
        invoices.add(new Invoice(
                "INV-104", "Freight Dispatch", 0, 140000.0,
                "EMP-002", "VEN-002", LocalDate.of(2026, 11, 11)));
        invoices.add(new Invoice(
                "INV-105", "Hardware Upgrade", 0, 310000.0,
                "EMP-001", "VEN-004", LocalDate.of(2026, 11, 12)));
        invoices.add(new Invoice(
                "INV-106", "Network Cabling", 0, 95000.0,
                "EMP-002", "VEN-003", LocalDate.of(2026, 11, 13)));
        invoices.add(new Invoice(
                "INV-107", "Warehouse Lease", 0, 210000.0,
                "EMP-001", "VEN-002", LocalDate.of(2026, 11, 14)));
        invoices.add(new Invoice(
                "INV-108", "Security Audit", 0, 185000.0,
                "EMP-002", "VEN-004", LocalDate.of(2026, 11, 15)));
        return invoices;
    }
}