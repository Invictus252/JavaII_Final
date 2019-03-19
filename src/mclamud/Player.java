
package mclamud;

import java.net.Socket;
import java.util.ArrayList;

public class Player {
    
    public String description = "A rather ordinary looking player.";
    public String name = "Nobody";
    public String password = "Password1";
    public int location = 144;
    public int armorClass =0;
    public int hitPoints = 0;
    public int acMOD = 0;
    public boolean NPC = false;
    public ArrayList<String> inventory = new ArrayList<>();
    public Socket socket;
    
    public Player(){}
     
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
        this.socket = sock;
    }

    


}