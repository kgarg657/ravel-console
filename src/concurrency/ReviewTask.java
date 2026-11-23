package concurrency;

import exception.CaseFullException;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class ReviewTask implements Runnable {
    private final CaseSlotManager slotManager;
    private final AtomicInteger grantedCount;
    private final AtomicInteger deniedCount;
    private final Random random;

    public ReviewTask(CaseSlotManager slotManager, AtomicInteger grantedCount, AtomicInteger deniedCount) {
        this.slotManager = slotManager;
        this.grantedCount = grantedCount;
        this.deniedCount = deniedCount;
        this.random = new Random();
    }

    @Override
    public void run() {
        String auditorName = Thread.currentThread().getName();
        int delay = 50 + random.nextInt(251);

        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        try {
            slotManager.claim(auditorName);
            grantedCount.incrementAndGet();
            System.out.println(auditorName + " -> GRANTED slot on " + slotManager.getCaseId());
        } catch (CaseFullException e) {
            deniedCount.incrementAndGet();
            System.out.println(auditorName + " -> DENIED (" + e.getMessage() + ")");
        }
    }
}
