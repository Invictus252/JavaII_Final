package mclamud;

import java.io.*;
import java.net.Socket; //new
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class World {
    private final static int MIN_PASSWORD_LENGTH = 8;
    private final static int MIN_PLAYERNAME_LENGTH = 3;
    private final static int MAX_PLAYERNAME_LENGTH = 15;
    private final static int NUMBER_DIRECTIONS = 6;
    public static Map<Integer, Area>areaMap = new HashMap<>();
    
    /************************************************************************
     * Displays an area to the remote player.
     * @param areaId An integer containing the ID of the area
     * @param p Player to return output to
     * @return boolean true = succeeded, false if not.
     ************************************************************************/
    public synchronized static boolean displayArea(int areaId, Player p) {
        boolean outcome = true;
        PrintWriter out = null;
        Area a = getArea(areaId);
        if (a != null && p != null){
            try {
                out = new PrintWriter(p.socket.getOutputStream(), true);
            } catch (IOException ex) {
                outcome = false;
            }

            if (outcome && out != null){
                String[] portals = {"north","south","west","east","up","down"};
                
                switch(a.gfx){
                    case "townSquare":
                        for(String x :GFX.townSquare){
                            out.println(x);
                        }
                        break;
                    case "buildingsWithManhole":
                        for(String x :GFX.buildingsWithManhole){
                            out.println(x);
                        }
                        break;
                    case "southCenter":
                        for(String x :GFX.southCenter){
                            out.println(x);
                        }
                        break;    
                    case "southEast":
                        for(String x :GFX.southEast){
                            out.println(x);
                        }
                        break;     
                    case "southWest":
                        for(String x :GFX.southWest){
                            out.println(x);
                        }
                        break;    
                    default:
                        break;
                }
                out.println(a.title.trim());
                out.println(a.description);

                //List the available items    
                out.print("Items available: | ");
                for(String x : a.items){
                    if(!x.equals(""))
                        out.print(x + " | ");
                }
                out.println();
                
                
                //List the available exits    
                out.print("Exits available: ");
                for (int i = 0; i < NUMBER_DIRECTIONS; i++){
                    if (a.exits[i] != 0){
                        out.print(portals[i] + " ");
                    }
                }
                out.println();
                //List the players in the area, excluding self
                for (Player areaPlayers : a.players.values()){
                    if (!areaPlayers.name.equalsIgnoreCase(p.name)){
                        out.println(areaPlayers.name + " is here.");
                    }
                }
                out.println(GFX.border);
                
                
            }
        }
        //out.println(GFX.border);
        return outcome;
    }
     
    /********************************************************************
     * Reads the area file specified in areaId and returns an area object
     * @param areaId An integer containing the ID of the area.
     * @return Area object
     ********************************************************************/ 
    private synchronized static Area readAreaFromFile(int areaId){
        Area a = new Area();
        String contents = null;
        String areaFile = "areas/" + String.valueOf(areaId) + ".area";
        File file = new File(areaFile);
        try{    
            if (file.exists()){
                contents = new String(Files.readAllBytes(Paths.get(areaFile)));
            } else {
                a = null;
                contents = null;
            }
        }
        catch(IOException e){
            System.out.println("An error occurred reading " + areaFile + ".");
        }
        if (a != null && contents != null){
            int indexNext;
            do {
                int indexStart = contents.indexOf("[");
                int indexEnd = contents.indexOf("]");
                String name = contents.substring(indexStart + 1, indexEnd).toLowerCase();
                indexNext = contents.indexOf("[", indexStart + 1);
                String value;
                if (indexNext < 0){
                    contents = contents.substring(indexEnd + 1);
                    value = contents;
                } else {
                    value = contents.substring(indexEnd + 1, indexNext);
                    contents = contents.substring(indexNext);
                }
                switch(name){
                    case "description":
                        a.description = value.trim();
                        break;
                    case "gfx":
                        a.gfx = value.trim();
                        break;    
                    case "title":
                        a.title = value.trim();
                        break;
                    case "exits":
                        String[] exits = value.trim().split("\\s*,\\s*");
                        for (int i = 0; i < exits.length; i++){
                            a.exits[i] = Integer.parseInt(exits[i]);
                        }
                        break;
                    case "items":
                        String[] items = value.trim().split("\\s*,\\s*");
                        a.items.addAll(Arrays.asList(items));
                        break;                
                    default:
                }
            } while(indexNext >= 0);
        }
    return a;
    }
    
    private synchronized static Player readPlayerFromFile(String pName){
        Player p = new Player();
        String contents = null;
        String playerFile = "players/" + pName + ".player";
        File file = new File(playerFile);
        try{    
            if (file.exists()){
                contents = new String(Files.readAllBytes(Paths.get(playerFile)));
            } else {
                System.out.println("NULL PLAYER LOADED");
                p = null;
                contents = null;
            }
        }
        catch(IOException e){
            System.out.println("An error occurred reading " + playerFile + ".");
        }
        if (p != null && contents != null){
            int indexNext;
            do {
                int indexStart = contents.indexOf("[");
                int indexEnd = contents.indexOf("]");
                String name = contents.substring(indexStart + 1, indexEnd).toLowerCase();
                indexNext = contents.indexOf("[", indexStart + 1);
                String value;
                if (indexNext < 0){
                    contents = contents.substring(indexEnd + 1);
                    value = contents;
                } else {
                    value = contents.substring(indexEnd + 1, indexNext);
                    contents = contents.substring(indexNext);
                }
                switch(name){
                    case "description":
                        p.description = value.trim();
                        break;
                    case "name":
                        p.name = value.trim();
                        break;    
                    case "password":
                        p.password = value.trim();
                        break;
                    case "location":
                        p.location = Integer.parseInt(value.trim());
                        break;
                    case "inventory":
                        String[] items = value.trim().split("\\s*,\\s*");
                        p.inventory.addAll(Arrays.asList(items));
                        break;                
                    default:
                }
            } while(indexNext >= 0);
        }
    return p;
    }
    /**********************************************************************
     * Determines whether the requested area exists as an area object, or
     * needs to be read from disk and dispatches accordingly.
     * @param areaId Integer containing the ID of the area.
     * @return Area object
     **********************************************************************/
    private synchronized static Area getArea(int areaId){
        Area a;
        if (areaMap.containsKey(areaId)){
            a = areaMap.get(areaId);
        } else { 
            a = readAreaFromFile(areaId);
            areaMap.put(areaId, a);
        }
        return a;
    }
    
    /***********************************************************************
     * Moves a player from one area to another
     * @param p Player object representing the connected player
     * @param newArea An int containing the area to which the player is 
     * being moved to.
     ***********************************************************************/
    public synchronized static void movePlayer(Player p, int newArea){
        Area a = getArea(p.location);
        a.players.remove(p.name.toLowerCase());
        Area b = getArea(newArea);
        b.players.put(p.name.toLowerCase(), p);
        p.location = newArea;
    }
    
    /************************************************************************
     * Removes a player object from an area. The area the player is being
     * removed from is in player.location.
     * @param p Player object being removed
     ************************************************************************/
    public synchronized static void removePlayer(Player p){
        Area a = getArea(p.location);
        a.players.remove(p.name.toLowerCase());
    }
    
    /************************************************************************
     * writePlayer saves the current Player object to a playername.area file.
     * @param p Player object being written to file
     * @return boolean indication failure or success
     ************************************************************************/
    public synchronized static boolean writePlayer(Player p){
        boolean outcome = true;  
        String path = "players/" + p.name.concat(".player").toLowerCase();
        File f = new File(path);
        String passwordHash = Integer.toHexString(p.password.hashCode());
        try{
            try (PrintWriter pw = new PrintWriter(f, "UTF-8")) {
                pw.println("[description]" + p.description);
                pw.println("[name]" + p.name);
                pw.println("[password]" + passwordHash);
                pw.println("[location]" + String.valueOf(p.location));
                pw.print("[inventory] ");
                int i = 0;
                p.inventory.forEach((x) -> {
                    if(i == p.inventory.size()){
                        pw.print(x);
                    }
                    else
                        pw.print(x +",");
                });
                pw.println();
            }
        } catch(FileNotFoundException | UnsupportedEncodingException e){
            outcome = false;
            System.out.println("An error occurred writing " + path + ".");
        }
        return outcome;
    }
    
    /************************************************************************
     * doesPlayerExist tests to see if an .area file exists for the player name.
     * @param playerName A string containing the player name
     * @return boolean - true if it exists and false if it does not.
     ************************************************************************/
    public synchronized static boolean doesPlayerExist(String playerName){
        File f = new File("players/" + playerName.concat(".player").toLowerCase());
        return f.exists();
    }
    
    /*************************************************************************
     * isValidPassword checks to see if password meets length and complexity
     * requirements, which are: 1 UPPER, 1 lower, and 1 number.
     * @param playerPassword
     * @return boolean - true if password meets requirements, false if not.
     *************************************************************************/
    public synchronized static boolean isValidPassword(String playerPassword){
        boolean outcome = true;
        int flags = 0;
        if (playerPassword.length() >= MIN_PASSWORD_LENGTH){
            for (int i = 0; i < playerPassword.length(); i++){
                if(Character.isUpperCase(playerPassword.charAt(i))){
                    flags = flags | 1;
                }
                if(Character.isLowerCase(playerPassword.charAt(i))){
                    flags = flags | 2;
                }
                if(Character.isDigit(playerPassword.charAt(i))){
                    flags = flags | 4;
                }
            }
            if (flags != 7){
                outcome = false;
            }
        } else {
            outcome = false;
        }
        return outcome;
    }
    
    /*************************************************************************
     * isValidPlayername checks to see if the player name meets length and 
     * character restrictions, which are: length, letters, numbers, and the
     * underscore _.
     * requirements, which are: 1 UPPER, 1 lower, and 1 number.
     * @param playerName String to be checked for validity
     * @return boolean - true if player name meets requirements, false if not.
     *************************************************************************/
    public synchronized static boolean isValidPlayername(String playerName){
        boolean outcome = true;
        int flags;
        if (playerName.length() >= MIN_PLAYERNAME_LENGTH && 
            playerName.length() <= MAX_PLAYERNAME_LENGTH){
            for (int i = 0; i < playerName.length(); i++){
                flags = 0;
                if(Character.isLetter(playerName.charAt(i))){
                    flags = flags | 1;
                }
                if(Character.isDigit(playerName.charAt(i))){
                    flags = flags | 2;
                }
                if(playerName.charAt(i) == '_'){
                    flags = flags | 4;
                }
                if (flags == 0){
                    outcome = false;
                    break;
                }
            }
        } else {
            outcome = false;
        }
        return outcome;
    }
    
// ************************************************************************
// Mine
//*************************************************************************
    
/**************************************************************************
 * doWalk parses the direction from the players command line and calculates the 
 * correct area to move the player to. The areas IDs are within the area 
 * object pointed to by player.location.
 * @param p Player object
 * @param direction command line string entered by player
 * @return boolean - true if direction points to valid area ID, false if not.
 */
    public synchronized static boolean doWalk(Player p, String direction){
        boolean outcome = false;
        String[] dirList = {"north","south","west","east","up","down"};
        String[] dirAbbr = {"n","s","w","e","u","d"};
        int areaIndex = -1;
        direction = direction.trim().toLowerCase();
        for (int i = 0; i < NUMBER_DIRECTIONS; i++){
            if (dirList[i].equals(direction) || dirAbbr[i].equals(direction)){
                areaIndex = i;
                break;
            }
        }
        if (areaIndex >= 0){
            Area a = getArea(p.location);
            if (a != null){
                if (a.exits[areaIndex] > 0){
                    sendMessageToArea(p, p.name + " has exited " + 
                        dirList[areaIndex] + "."); //new
                    movePlayer(p, a.exits[areaIndex]);
                    sendMessageToArea(p, p.name + " has arrived."); //new
                    outcome = true;
                }
            }
        }
        return outcome;        
    }
    
    public synchronized static boolean checkDirection(Player p, String direction){
        boolean outcome = false;
        String[] dirList = {"north","south","wet","east","up","down"};
        String[] dirAbbr = {"n","s","w","e"};
        int areaIndex = -1;
        direction = direction.trim().toLowerCase();
        for (int i = 0; i < NUMBER_DIRECTIONS; i++){
            if (dirList[i].equals(direction) || dirAbbr[i].equals(direction)){
                areaIndex = i;
                break;
            }
        }
        if (areaIndex >= 0){
            Area a = getArea(p.location);
            if (a != null){
                outcome = true;
            }
        }
        return outcome;        
    }

/**************************************************************************
 * doLook looks into adjoining rooms and retrieves their descriptions
 * @param out PrintWriter in use
 * @param p Current player...used for reference
 * @param direction room to be displayed
 */
    public synchronized static void doLook(PrintWriter out,Player p,String direction) {
        String[] dirList = {"north","south","west","east","up","down"};
        String[] dirAbbr = {"n","s","w","e","u","d"};
        int areaIndex = -1;
        direction = direction.trim().toLowerCase();
        for (int i = 0; i < NUMBER_DIRECTIONS; i++){
            if (dirList[i].equals(direction) || dirAbbr[i].equals(direction)){
                areaIndex = i;
                break;
            }
        }
        if (areaIndex >= 0){
            Area a = getArea(p.location);
            String curPlayers = "Current Players  -> | ";
            String[] portals = {"north","south","west","east","up","down"};
            String curExits = "Available Exits -> | ";
            if (a != null){
                if (a.exits[areaIndex] > 0){
                    Area b = getArea(a.exits[areaIndex]);
                    
                    out.println(b.description);
                    for(String name : b.players.keySet()){
                        curPlayers += name + " | ";
                    }
                    for (int i = 0; i < NUMBER_DIRECTIONS; i++){
                        if (b.exits[i] != 0){
                            curExits += portals[i] + " | ";
                        }
                    }
                    out.println(curExits);
                    out.println(curPlayers);
                    }
                else if(a.exits[areaIndex] == 0)
                    out.println("Nothing to see.");
            }

        }

    }
        
    public synchronized static boolean sendMessageToArea(Player p, String message){ //new
        boolean outcome = true;
        Area a = getArea(p.location);
        PrintWriter out;
        if (a != null){
            for (Player areaPlayers : a.players.values()){
                try {
                    if (!areaPlayers.name.equalsIgnoreCase(p.name)&& !areaPlayers.NPC){
                        Socket psock = areaPlayers.socket;
                        out = new PrintWriter(psock.getOutputStream(), true);
                        out.println(p.name + " ]--> " + message);
                    }
                } catch (IOException e) {
                    outcome = false;
                }
            }
        } else {
            outcome = false;
        }
        return outcome;
    }
       
    public synchronized static boolean sendMessageToPlayer(Player p,String P, String message){ //new
        boolean outcome = true;
        Area a = getArea(p.location);
        PrintWriter out;
        if (a != null){
            for (Player areaPlayers : a.players.values()){
                try {
                    if (!areaPlayers.name.equalsIgnoreCase(p.name)&& areaPlayers.name.equals(P)){
                        Socket psock = areaPlayers.socket;
                        out = new PrintWriter(psock.getOutputStream(), true);
                        out.println(p.name + " ]SHHHHH... " + message);
                    }
                } catch (IOException e) {
                    outcome = false;
                }
            }
        } else {
            outcome = false;
        }
        return outcome;
    }
    
    public synchronized static boolean sendEmotetoPlayer(Player p,String P, String emote){ //new
        String emoteOut ="";
        boolean outcome = true;
        Area a = getArea(p.location);
        PrintWriter out;
        switch(emote){
            case "wink":
                emoteOut += "*" + p.name + " winks at you ;)";
                break;
            case "smile":
                emoteOut += "*" + p.name + " smiles at you :)";
                break;
            case "frown":
                emoteOut += "*" + p.name + " frowns at you :(";
                break;
            default:
                
                break;
        }
        if (a != null){
            for (Player areaPlayers : a.players.values()){
                try {
                    if (!areaPlayers.name.equalsIgnoreCase(p.name)&& areaPlayers.name.equals(P)){
                        Socket psock = areaPlayers.socket;
                        out = new PrintWriter(psock.getOutputStream(), true);
                        out.println(emoteOut);
                    }
                } catch (IOException e) {
                    outcome = false;
                }
            }
        } else {
            outcome = false;
        }
        return outcome;
    }
    
    /***********************************************************************************
     * helpMe can be used for command explaining or command direction
     * @param out Current PrintWriter
     * @param command command being referenced
     * @param err Boolean used to establish what level of dialogue is needed
     */
    public synchronized static void helpMe(PrintWriter out,String command,boolean err){
        String[] helpLine = {"",""};
        switch(command){
            case "look":
                helpLine[0] = "Inspect surroundings/others/items";
                helpLine[1] = "Command format is: look <name> || look <direction> || look";
                break;
            case "walk":
                helpLine[0] = "Walk/go about";
                helpLine[1] = "Command format is: walk/go <direction>";
                break;    
            case "say":
                helpLine[0] = "Speak to room";
                helpLine[1] = "Command format is: say <message>";
                break;
            case "whisper":
                helpLine[0] = "Speak to person";
                helpLine[1] = "Command format is: whisper <name> <message>";
                break;
            case "describe":
                helpLine[0] = "Describes yourself for the others";
                helpLine[1] = "Command format is: describe <message>";
                break;
            case "take":
            case "get":
                helpLine[0] = "Retrieve item from room";
                helpLine[1] = "Command format is: take/get <item>";
                break;    
            case "drop":
                helpLine[0] = "Drop item in room";
                helpLine[1] = "Command format is: drop <item>";
                break;    
            case "emote":
                helpLine[0] = "Sends emote to person in room";
                helpLine[1] = "Command format is: emote <person> <emote>";
                break;    
            case "stats":
                helpLine[0] = "See player stats";
                helpLine[1] = "Command format is: stats|/s || stats|/s <person> ";
                break;    
            case "help":
                helpLine[0] = "See command info";
                helpLine[1] = "Command format is: help <command> ";
                break;    
        }
        if(err)
            out.println(helpLine[1]);
        else{
            for(String x : helpLine){
                out.println(x);
            }    
        }
    }
    
    public synchronized static void getItem(String item,Player p){
        Area a = getArea(p.location);
        if(a.items.contains(item)){
           p.inventory.add(item);
           a.items.remove(item); 
        }
    } 
    
    public synchronized static void dropItem(String item,Player p){
        Area a = getArea(p.location);
        if(p.inventory.contains(item)){
            p.inventory.remove(item);
            a.items.add(item);
        }
        
            
    }    
    
    public synchronized static Player loadPlayer(String name){
        Player x = readPlayerFromFile(name);
        return x;
    }
    
    public synchronized static void listInventory(PrintWriter out,Player p){
        out.print("Current Inventory ----[ | ");
        if(p.inventory.isEmpty()){
            out.println(" | ]");
        }
        else{
            p.inventory.forEach((x) -> {
                out.print(x + " | ");
            }); 
            out.println(" ]");
        }
    }
    
    public synchronized static void npcResponse(PrintWriter out,Player p,String P2name,String message){
        Area a = getArea(p.location);
        Boolean found = false;
        for(Player x : a.players.values()){
            if(x.name.equals(P2name)){
                found = true;
                switch(P2name){
                    case "Moe":
                    case "Larry":
                    case "Curly":
                        out.println(Stooges.talkBack(P2name,message));
                        break;
                }
            }
        }
        if(!found)
            out.println("They dont seem to hear you. How was all that acid?");  
    }
    
    public synchronized static int getDieRoll(int sides){
        int roll = (int) (Math.random() * sides) + 1;
        return roll;
    }
    
    public synchronized static void showPlayer(PrintWriter out,Player p){
        String[] playerSheet ={" ________________________",
                               "| Name  : " + p.name,
                               "| HP    : " + p.getHP(),
                               "| AC    : " + p.getAC(),
                               "| ACmod : " + p.getACmod(),
                               "| Info  : " + p.description,
                               "|________________________"};        
        for (String strTemp : playerSheet){
            out.println(strTemp);
        }
    }
    
    public synchronized static void showPlayer(PrintWriter out,Player p,String playerName){
        Area a = new Area();
        Player X = null;
        a = getArea(p.location);
        for(Player x : a.players.values()){
            if(x.name.equalsIgnoreCase(playerName))
                X = x;
        }
        
        String[] playerSheet ={" ________________________",
                               "| Name  : " + X.name,
                               "| HP    : " + X.getHP(),
                               "| AC    : " + X.getAC(),
                               "| ACmod : " + X.getACmod(),
                               "| Info  : " + X.description,
                               "|________________________"};        
        for (String strTemp : playerSheet){
            out.println(strTemp);
        }
    }
    
    public synchronized static Player doBattle(Player p1, Player p2){
        boolean rollCall = false;
        Area a = new Area();
        a = World.getArea(p1.location);
        //Player x = null;
        for(Player X : a.players.values()){
            if(X.name.equals(p2.name)|| p2.name.equals("Nobody")) {
                rollCall = true;
                //x = X;
            }
        }
        if(rollCall){
            while(p1.getHP() > 0 && p2.getHP() > 0){
                int attack = (int) (Math.random() * 20 + 1);
                if (attack >= p2.getAC()){
                    int damage = p1.getDamage();
                    System.out.println(p1.name + " hit " + p2.name + " for " + damage + " points.");
                    p2.setHP(p2.getHP() - damage);
                } else {
                    System.out.println(p1.name + " missed " + p2.name + "."); 
                }

                try {Thread.sleep(500);}catch(Exception e){}

                attack = (int) (Math.random() * 20 + 1);
                if (attack >= p1.getAC()){
                    int damage = p2.getDamage();
                    System.out.println(p2.name + " hit " + p1.name + " for " + damage + " points.");
                    p1.setHP(p1.getHP() - damage);
                } else {
                    System.out.println(p2.name + " missed " + p1.name + "."); 
                }

                try {Thread.sleep(500);}catch(Exception e){}
            }
        }
        
        
        if (p1.getHP() > 0){ 
            return p1; 
        } else {
            return p2;
        } 
        
    }
}

