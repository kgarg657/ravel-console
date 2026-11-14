package model;

public abstract class Entity implements HasId {
    protected String id;
    protected String name;
    protected int riskScore;
    protected EntityType type;

    public Entity(String id, String name, int riskScore, EntityType type) {
        this.id = id;
        this.name = name;
        this.riskScore = riskScore;
        this.type = type;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public EntityType getType() {
        return type;
    }

    public abstract String describe();
}