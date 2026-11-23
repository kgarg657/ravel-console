package rules;

import model.Contactable;

public class SharedBankRule extends SharedFieldRule{
    
    @Override 
    public String getRuleName(){
        return "Shared Bank Account";
    }

    @Override 
    protected  String extract(Contactable c){
        return c.getBankAccount();
    }

    @Override 
    protected String label(){
        return "SHARED_BANK";
    }

    @Override
    protected String normalize(String raw){
         if (raw == null){
            return null;
        }
        StringBuilder cleaned = new StringBuilder();
        raw = raw.toUpperCase();
        for (int i = 0; i<raw.length(); i++){
            char ch = raw.charAt(i);
            if (!Character.isLetterOrDigit(ch)){
                continue;
            }
            cleaned.append(ch);
        }
        return cleaned.toString();
    }
}
