package concurrency;

import java.util.concurrent.atomic.AtomicInteger;
import util.ConsoleInput;

public class ConcurrencyDemo {

    public static void run() {
        ConsoleInput input = new ConsoleInput();

        while (true) {
            System.out.println("\n=== Auditor Slot Concurrency Demo ===");
            System.out.println("1. Run Safe Slot Manager (synchronized, max 2 slots)");
            System.out.println("2. Run Unsafe Slot Manager (race condition demo)");
            System.out.println("0. Back to Case Menu");

            int choice = input.readInt("Choose demo mode: ", 0, 2);
            if (choice == 0) {
                return;
            }

            CaseSlotManager manager;
            if (choice == 1) {
                manager = new CaseSlotManager("CASE-01", 2);
            } else {
                manager = new UnsafeSlotManager("CASE-01", 2);
            }

            AtomicInteger granted = new AtomicInteger(0);
            AtomicInteger denied = new AtomicInteger(0);

            Thread[] threads = new Thread[5];
            for (int i = 0; i < 5; i++) {
                ReviewTask task = new ReviewTask(manager, granted, denied);
                threads[i] = new Thread(task, "Auditor-" + (i + 1));
            }

            System.out.println("\nStarting 5 auditor threads for " + manager.getCaseId() + "...");

            for (int i = 0; i < 5; i++) {
                threads[i].start();
            }

            for (int i = 0; i < 5; i++) {
                try {
                    threads[i].join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            System.out.println("\n--- Run Summary ---");
            System.out.println("Granted : " + granted.get());
            System.out.println("Denied  : " + denied.get());
            System.out.println("Assigned: " + manager.getAssigned() + " / " + manager.getMaxAuditors());
        }
    }
}