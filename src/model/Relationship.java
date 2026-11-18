package model;

import exception.InvalidEntityException;

public class Relationship implements HasId{
    private String linkId;
    private String sourceId;
    private String targetId;
    private String label;
    private RelationshipStatus status;
    private Double amount;

    public Relationship(String linkId, String sourceId, String targetId, String label, RelationshipStatus status, Double amount){
        if (linkId != null){
            linkId = linkId.trim();
            if(linkId.isEmpty()){
                throw new InvalidEntityException("Link ID cannot be blank.");
            }
        }
        if (sourceId == null || sourceId.trim().isEmpty()){
            throw new InvalidEntityException("Source entity ID cannot be blank.");
        }
        if (targetId == null || targetId.trim().isEmpty()){
            throw new InvalidEntityException("Target entity ID cannot be blank.");
        }
        if (label == null || label.trim().isEmpty()){
            throw new InvalidEntityException("Relationship label cannot be blank.");
        }
        if (status == null){
            throw new InvalidEntityException("Relationship status cannot be null.");
        }
        if ((sourceId.trim()).equalsIgnoreCase(targetId.trim())){
            throw new InvalidEntityException("Self-Links are not allowed: "+targetId.trim());
        }


        this.linkId = linkId;
        this.targetId = targetId.trim();
        this.sourceId = sourceId.trim();
        this.label = label.trim();
        this.status = status;
        this.amount = amount;
    }

    public Relationship(String linkId, String sourceId, String targetId, String label ,RelationshipStatus status){
        this(linkId, sourceId, targetId, label, status, null);
    }

    public Relationship(String sourceId, String targetId, String label, RelationshipStatus status){
        this(null, sourceId, targetId, label, status, null);
    }

    @Override 
    public String getId(){
        return linkId;
    }
    public String getSourceId(){
        return sourceId;
    }
    public String getTargetId(){
        return targetId;
    }
    public String getLabel(){
        return label;
    }
    public RelationshipStatus getStatus(){
        return status;
    }
    public Double getAmount(){
        return amount;
    }

    public void setId(String linkId){
        if(linkId!= null){
            this.linkId = linkId.trim();
            if(this.linkId.isEmpty()){
                throw new InvalidEntityException("Link ID cannot be blank.");
            }
        }
    }

    public void setStatus(RelationshipStatus status){
        if (status == null){
            throw new InvalidEntityException("Relationship status cannot be null.");
        }
        this.status = status;
    }
    public String key(){
        return (sourceId+"|"+targetId+"|"+label);
    
    }


    
}
