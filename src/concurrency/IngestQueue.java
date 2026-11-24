package concurrency;

import java.util.LinkedList;
import java.util.Queue;
import model.Invoice;

public class IngestQueue {
    private static final int CAPACITY = 3;

    private final Queue<Invoice> queue = new LinkedList<>();
    private boolean finished = false;

    public synchronized void put(Invoice invoice) throws InterruptedException {
        if (invoice == null) {
            throw new IllegalArgumentException("Invoice cannot be null.");
        }
        while (queue.size() >= CAPACITY) {
            wait();
        }
        queue.add(invoice);
        notifyAll();
    }

    public synchronized Invoice take() throws InterruptedException {
        while (queue.isEmpty() && !finished) {
            wait();
        }
        if (queue.isEmpty() && finished) {
            return null;
        }
        Invoice invoice = queue.poll();
        notifyAll();
        return invoice;
    }

    public synchronized void finish() {
        finished = true;
        notifyAll();
    }
}