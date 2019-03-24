
package mclamud;

import java.net.Socket;
import java.util.ArrayList;

public class Player {
    
    public String description = "A rather ordinary looking player.";
    public String name = "Nobody";
    public String password = "Password1";
    public int location = 144;
    public int armorClass =10;
    public int hitPoints = 50;
    public int scanCTR =0;
    public int virusCTR =0;
    public int trojanCTR =0;
    public int scriptCTR = 0;
    public int wormCTR = 0;
    private int XP = 0;
    public int acMOD = 0;
    public boolean NPC = false;
    public boolean FIGHT = false;
    public boolean isLoggedON = false;
    public ArrayList<String> inventory = new ArrayList<>();
    public Socket socket;
    
    public Player(){
        for(String x : inventory){
            switch(x){
                case "scan":
                    scanCTR++;
                    break;
                case "worm":
                    wormCTR++;
                    break; 
                case "trojan":
                    trojanCTR++;
                    break;
                case "virus":
                    virusCTR++;
                    break;
                case "script":
                    scriptCTR++;
                    break;
                default:
                    break;
            }
        }        
    }
     
    public void setAC(int ac){
        armorClass = ac;
        acMOD = getModAC(armorClass);
        
    }
    public void setHP(int hp){
        hitPoints = hp;
    }
    public int getAC(){
        return armorClass;
    }
    public int getHP(){
        return hitPoints;
    }
    public int getACmod(){
        return acMOD;
    } 
    private static int getModAC(int score){
        return (score / 2 - 5);
    } 
    public int getDamage(){
        int damage = (int) (Math.random() * 6 + 1);
        return damage;        
    }
    
    public Player(Socket sock){
        socket = sock;
    }

    /**
     * @return the XP
     */
    public int getXP() {
        return XP;
    }

    /**
     * @param XP the XP to set
     */
    public void setXP(int XP) {
        this.XP = XP;
    }

    


}