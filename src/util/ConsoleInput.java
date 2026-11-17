package util;

import java.util.Scanner;

public class ConsoleInput {
    private final Scanner sc = new Scanner(System.in);
    public String readNonBlank(String prompt){
        while(true){
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()){
                return input;
            }
            System.out.println("[Error] Input cannot be blank. Try again.");
        }
    }

    public int readInt(String prompt, int min, int max){
        while (true) {
        System.out.print(prompt);
        String raw = sc.nextLine().trim();
        try{
            int value = Integer.parseInt(raw);
            if(value>= min && value<=max){
                return value;
            }
            System.out.println(" [Error] Enter a no. between "+min+" and "+max+ ".");
        } catch(NumberFormatException e){
            System.out.println(" [Error] Invalid Integer. Please enter digits only.");
        }
    }
}

public double readDouble(String prompt){
    while (true) {
        System.out.print(prompt);
        String raw = sc.nextLine().trim();
        try{
            double value = Double.parseDouble(raw);
            if (Double.isFinite(value)){
                return value;
            }
            System.out.println("[Error] Number must be finite.");
        } catch(NumberFormatException e){
            System.out.println(" [Error] Invalid decimal number. Try again.");
        }
    }
}

public boolean readYesNo(String prompt){
    while(true){
        System.out.print(prompt);
        String input =sc.nextLine().trim().toLowerCase();
        if (input.equals("y") || input.equals("yes")){
            return true;
        }
        if (input.equals("n")|| input.equals("no")){
            return  false;
        }
        System.out.println(" [Error] Please enter 'y' or 'n'.");
    }
}


}
