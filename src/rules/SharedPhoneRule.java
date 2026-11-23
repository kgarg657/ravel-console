package rules;

import model.Contactable;

public class SharedPhoneRule extends SharedFieldRule{

    @Override 
    public String getRuleName(){
        return "Shared Phone";
    }

    @Override 
    protected  String extract(Contactable c){
        return c.getPhone();
    }

    @Override 
    protected  String label(){
        return "SHARED_PHONE";
    }
    
}
