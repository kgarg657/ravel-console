import model.Entity;
import model.Relationship;
import repository.InMemoryRepository;
import repository.Repository;
import service.EntityService;
import service.RelationshipService;
import ui.EntityMenu;
import ui.RelationshipMenu;
import util.ConsoleInput;
import util.IdGenerator;

public class RavelApp {
    public static void main(String[] args) {
        Repository<Entity> entityRepo = new InMemoryRepository<>();
        Repository<Relationship> linkRepo = new InMemoryRepository<>();
        IdGenerator idGenerator = new IdGenerator();
        ConsoleInput consoleInput = new ConsoleInput();

        RelationshipService relationshipService = new RelationshipService(
                linkRepo, entityRepo, idGenerator);
        EntityService entityService = new EntityService(
                entityRepo, relationshipService);

        EntityMenu entityMenu = new EntityMenu(
                entityService, relationshipService, consoleInput);
        RelationshipMenu relationshipMenu = new RelationshipMenu(
                relationshipService, consoleInput);

        while (true) {
            System.out.println("\n=== Ravel Console ===");
            System.out.println("1. Entity Menu");
            System.out.println("2. Relationship Menu");
            System.out.println("3. Fraud & Risk Rules (coming in M3/M4)");
            System.out.println("4. Graph Analysis (coming in M3/M4)");
            System.out.println("5. Audit Cases (coming in M3/M4)");
            System.out.println("0. Exit");

            int choice = consoleInput.readInt("Choose an option: ", 0, 5);
            switch (choice) {
                case 1:
                    entityMenu.show();
                    break;
                case 2:
                    relationshipMenu.show();
                    break;
                case 3:
                case 4:
                case 5:
                    System.out.println("coming in M3/M4");
                    break;
                case 0:
                    System.out.println("Exiting Ravel Console.");
                    return;
                default:
                    break;
            }
        }
    }
}
