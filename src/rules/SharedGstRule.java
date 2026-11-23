package rules;

import model.Contactable;

public class SharedGstRule extends SharedFieldRule {

    @Override 
    public String getRuleName(){
        return "Shared GST";
    }

    @Override 
    protected String extract(Contactable c){
        return c.getGst();
    }

    @Override 
    protected String label(){
        return "SHARED_GST";
    }
}
