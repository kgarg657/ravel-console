package model;

import exception.InvalidEntityException;

public abstract class Entity implements HasId {
    protected String id;
    protected String name;
    protected int riskScore;
    protected EntityType type;

    protected Entity(String id, String name, int riskScore, EntityType type) {
        if (riskScore < 0 || riskScore > 100) {
            throw new InvalidEntityException("Risk score must be between 0 and 100.");
        }
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

    public void addRisk(int delta) {
        int updated = this.riskScore + delta;
        if (updated < 0) {
            this.riskScore = 0;
        } else if (updated > 100) {
            this.riskScore = 100;
        } else {
            this.riskScore = updated;
        }
    }

    public String getRiskTier() {
        if (riskScore >= 70) {
            return "HIGH";
        }
        if (riskScore >= 40) {
            return "MEDIUM";
        }
        return "LOW";
    }

    public abstract String describe();
}