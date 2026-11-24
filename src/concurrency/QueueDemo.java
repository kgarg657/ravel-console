package concurrency;

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
    }
}