package main;

import model.AuditCase;
import model.Entity;
import model.Relationship;
import repository.InMemoryRepository;
import repository.Repository;
import rules.SharedBankRule;
import rules.SharedGstRule;
import rules.SharedPhoneRule;
import service.CaseService;
import service.DetectionService;
import service.EntityService;
import service.GraphService;
import service.RelationshipService;
import ui.CaseMenu;
import ui.EntityMenu;
import ui.GraphMenu;
import ui.RelationshipMenu;
import util.ConsoleInput;
import util.IdGenerator;

public class RavelApp {

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput();
        IdGenerator idGenerator = new IdGenerator();

        Repository<Entity> entityRepo = new InMemoryRepository<>();
        Repository<Relationship> linkRepo = new InMemoryRepository<>();
        Repository<AuditCase> caseRepo = new InMemoryRepository<>();

        RelationshipService relationshipService = new RelationshipService(linkRepo, entityRepo, idGenerator);
        EntityService entityService = new EntityService(entityRepo, relationshipService);

        CaseService caseService = new CaseService(caseRepo, entityRepo, idGenerator);
        entityService.setCaseService(caseService);

        // DetectionService + rules + GraphService + GraphMenu wiring
        DetectionService detectionService = new DetectionService(relationshipService, idGenerator);
        detectionService.addRule(new SharedGstRule());
        detectionService.addRule(new SharedPhoneRule());
        detectionService.addRule(new SharedBankRule());

        GraphService graphService = new GraphService(relationshipService);

        EntityMenu entityMenu = new EntityMenu(entityService, relationshipService, input);
        RelationshipMenu relationshipMenu = new RelationshipMenu(relationshipService, input);
        GraphMenu graphMenu = new GraphMenu(
                detectionService,
                graphService,
                relationshipService,
                entityService,
                input);
        CaseMenu caseMenu = new CaseMenu(caseService, input);

        while (true) {
            System.out.println("\n=== RAVEL FORENSIC AUDIT CONSOLE (M3) ===");
            System.out.println("1. Entity Menu (Registry, Reports, Load Demo Data)");
            System.out.println("2. Detection & Graph Analysis Menu (Options 2-6)");
            System.out.println("3. Relationship Menu");
            System.out.println("4. Case & Concurrency Menu (Options 7, 9, 10)");
            System.out.println("0. Exit");

            int choice = input.readInt("Choose an option: ", 0, 4);

            switch (choice) {
                case 1:
                    entityMenu.show();
                    break;
                case 2:
                    graphMenu.show();
                    break;
                case 3:
                    relationshipMenu.show();
                    break;
                case 4:
                    caseMenu.show();
                    break;
                case 0:
                    System.out.println("Exiting Ravel.");
                    return;
                default:
                    break;
            }
        }
    }
}