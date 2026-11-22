package rules;
import java.util.List;
import model.Entity;
import model.Relationship;

public interface RiskRule {
    String getRuleName();
    List<Relationship> detect(List<Entity> all);
}
