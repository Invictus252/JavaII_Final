package mclamud;


public class Stooges extends Player {
    
      
    public Stooges(){
        this.description = "A Stooge";
        this.armorClass=0;
        this.hitPoints=30;
        this.NPC = true;
    }
       
    public static String talkBack(String name,String req){
        String res = "";
        switch(name){
            case "Moe":
                switch(req){
                    case "weather":
                        res = "My weather rock app is on the fritz partner";
                        break;
                    case "joke":
                        res = "You are trying to find humor in an inanimate piece of code";
                        break;
                    case "name":
                        res = "The name is Moe";
                        break;    
                    default:
                        res = "What the hell did you say!?!";
                        break;
                }
                break;
            case "Larry":
                switch(req){
                    case "weather":
                        res = "How about weather or not you're an idiot";
                        break;
                    case "joke":
                        res = "You..just You.";
                        break;
                    case "name":
                        res = "The name is Larry";
                        break;    
                    default:
                        res = "Why I outta!!";
                        break;
                }
                break;
            case "Curly":
                switch(req){
                    case "weather":
                        res = "weather what?";
                        break;
                    case "joke":
                        res = "You see there is this bird..";
                        break;
                    case "name":
                        res = "The name is Curly";
                        break;
                    default:
                        res = "Oh hey there!";
                        break;
                }
                break;
//            case "Shemp":
//                switch(req){
//                    case "weather":
//                        res = "My weather rock app is on the fritz partner";
//                        break;
//                    case "joke":
//                        res = "You are trying to find humor in an inanimate piece of code";
//                        break;
//                    default:
//                        res = "What the hell did you say!?!";
//                        break;
//                }
//                break;
            default:
                break;
        }
        
        
        return res;
    }
    

 }
