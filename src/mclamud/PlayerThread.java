package mclamud;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class PlayerThread implements Runnable {
    
    
    private final Socket psock;
    private final Player player;
    private static boolean haxor;
    private final Stooges npc = new Stooges();
    private final int[] filesForNPC = {141,142,143,144,145,
                                       146,147,148,149,1412,
                                       1413,1452,1453,1462,1463};

    private static Scanner in;
    
    
 
    public PlayerThread(Socket sock,String type){
        switch(type){
            case "NPC":
                this.psock = sock;
                npc.name="NEO";
                npc.NPC = true;
                npc.location = 148;
                this.player = npc;
                break;
            case "NPC2":
                this.psock = sock;
                npc.name="MORPHEUS";
                npc.NPC = true;
                npc.location = 144;
                this.player = npc;
                break;    
            default:
                this.psock= sock;
                this.player = new Player(this.psock);                
                break;
            
        }
    }

    @Override
    public void run() {  
        PrintWriter out = null;
        String playerIn;
        boolean exit = false;
        try {
            if(player.NPC){
                Area a = new Area();
                a.players.put(player.name, player);
                //System.out.println("INI Player MAP" + a.players);
                World.movePlayer(player, player.location);
            }else{
                in = new Scanner(psock.getInputStream());
                out = new PrintWriter(psock.getOutputStream(), true);                
                if (!playerLogin(in, out)){    //Login                
                    exit = true;                    
                } else { //Successful login
                    //DUPLICATION NCHECK HERE
                    Server.curUsers.add(player);
                    player.NPC = false;
                    System.out.println(Server.curUsers);
                    Area a = World.getArea(player.location);
                    World.movePlayer(player, player.location);
                    World.sendMessageToArea(player, player.name + " has arrived.");
                    World.displayArea(player.location, player);
                }
                while(!exit){ //Main loop
                    if(player.NPC){
                        // MOVE NPCs?
                    }else{
                        playerIn = in.nextLine();
                        exit = commandDispatcher(playerIn, out);
                    }

                }       

                //Player has left world
                if(haxor==true){
                    Player sys = new Player();
                    sys.name = "Sys Admin";
                    World.sendMessageToWorld(sys,"Sys Admin denial @ "+ player.name); 
                    exitPlayer();
                }
                else{
                    World.sendMessageToArea(player, player.name + " has returned to reality.");  
                    exitPlayer();
                }
                
               
            }        
        } catch (IOException | NoSuchElementException e) {
            //Connection has been lost
            World.sendMessageToArea(player, player.name + " crumbles into dust.");
            exitPlayer();
        }
    }
    
    private void exitPlayer(){
        try {
            if(player.NPC || haxor==true){
                if(haxor==true){
                    System.out.println("Attack prevented! @ exitPlayer");
                }
                else{
                    System.out.println("NPC disconnected.");
                }
            }else{
                Server.curUsers.remove(player);
                String ip = psock.getInetAddress().toString();
                System.out.println(ip + " disconnected.");
                System.out.println(Server.curUsers);
                psock.close();                      
            }
        } catch (IOException ex){ 
            System.out.println("An IOException occurred when a player exited.");
        }
        finally{
            if(haxor==false)
                World.removePlayer(player);
        }
    }
    
    private boolean commandDispatcher(String command, PrintWriter out){
        boolean exitFlag = false;
        command = command.trim();
        String[] tokens= {};
        if (command.length() > 0){
            if(command.length() > 2)
                tokens = command.split(" ", 3);
            else if(command.length() <= 2)
                tokens = command.split(" ",2);
            else
                tokens[0] = command;
            Stooges punchingBag = new Stooges();
            OUTER:
            switch (tokens[0].toLowerCase()) {
                case "menu":
                    //FUNCTION in,out
                    break;
                case "fight":
                case "battle":
                case "/b":
                    switch(tokens.length){
                        case 1:
                            World.doBattle(player,punchingBag);
                            break OUTER;
                        case 2:
                            switch(tokens[1]){
                                
                            }
                            break OUTER;
                        case 3:
                            switch(tokens[1]){
                                case "stand":
                                    switch(tokens[2]){
                                        case "ready":
                                            player.FIGHT = true;
                                            player.lastAction = "Fighter Ready!";
                                            break OUTER;
                                        case "down":
                                            player.FIGHT = false;
                                            player.lastAction = "Fighter Standing Down...";
                                            break OUTER;
                                        default:
                                            break OUTER;
                                    }
                                case "check":
                                    // FIGHTER COMPARISON FUNCTION in,out,player,p2name
                                    break OUTER;
                                default:
                                    break OUTER;                            }
                    }
                case "stats":
                case "/s":
                    switch(tokens.length){
                        case 1:
                            World.showPlayer(out, player);
                            setStage(in,out,false,true);
                            break OUTER;
                        case 2:
                            World.showPlayer(out, player, tokens[1]);
                            setStage(in,out,false,true);
                            break OUTER;
                        default:
                            World.helpMe(out, tokens[0], true);
                            setStage(in,out,false,true);
                            break OUTER;
                    }
                case "npc":
                case "/n":
                    if(tokens.length == 3){
                        World.npcResponse(out, player, tokens[1], tokens[2]);
                        setStage(in,out,false,true);
                    } else {
                        //World.helpMe(out,tokens[0],true);
                    }
                    break;
                case "inventory":
                case "/i":
                    World.listInventory(out, player);
                    break;
                case "walk":
                case "go":
                case "w":
                case "g":
                    if (tokens.length > 1 && !"".equals(tokens[1])){
                        if (World.doWalk(player, tokens[1])){
                            World.displayArea(player.location, player);
                        } else {
                            out.println("You can't go in that direction.");
                        }
                    } else {
                        World.helpMe(out,"walk",true);
                    }
                    break;
                case "look":
                case "/l":
                    switch (tokens.length) {
                        case 1:
                            GFX.clearScreen(out);
                            World.displayArea(player.location, player);
                            break OUTER;
                        case 2:
                            switch (tokens[1]){
                                case "north":
                                case "n":
                                case "south":
                                case "s":
                                case "west":
                                case "w":
                                case "east":
                                case "e":
                                case "up":
                                case "u":
                                case "down":
                                case "d":
                                    GFX.clearScreen(out);
                                    World.doLook(out, player,tokens[1]);
                                    setStage(in,out,false,true);
                                    break OUTER;
                                default:
                                    Area a = World.areaMap.get(player.location);
                                    for (Player x : a.players.values()){
                                        if (x.name.equalsIgnoreCase(tokens[1])){
                                            out.println(x.description);
                                        }
                                        else
                                            World.helpMe(out,tokens[0],true);
                                    }
                                    break OUTER;
                            }
                        default:
                            World.helpMe(out,tokens[0],true);
                            break OUTER;
                    }
                case "say":
                    System.out.println(tokens.length);
                    switch (tokens.length) {
                         case 2:
                            World.sendMessageToArea(player,tokens[1]);
                            break OUTER;
                        case 3:
                            World.sendMessageToArea(player,tokens[1] + " " + tokens[2]);
                            break OUTER;
                        default:
                            World.helpMe(out,tokens[0],true);
                            break OUTER;
                    }
                case "whisper":
                case "/w":
                    if(tokens.length > 2){
                        World.sendMessageToPlayer(player,tokens[1],tokens[2]);
                    } else {
                        World.helpMe(out,tokens[0],true);
                    }
                    break;  
                case "emote":
                case "/e":
                    if(tokens.length > 2){
                        World.sendEmotetoPlayer(player,tokens[1],tokens[2]);
                    } else {
                        World.helpMe(out,tokens[0],true);
                    }
                    break;                     
                case "get":
                case "take":
                    if(tokens.length > 1 ){
                        World.getItem(tokens[1], player);
                        
                    } else {
                        World.helpMe(out,tokens[0],true);
                    }
                    World.writePlayer(player);
                    GFX.clearScreen(out);
                    World.displayArea(player.location, player);
                    break;    
                case "drop":
                    if(tokens.length > 1 && player.inventory.contains(tokens[1])){
                        World.dropItem(tokens[1], player);
                        
                    } else {
                        World.helpMe(out,tokens[0],true);
                    }
                    World.writePlayer(player);
                    GFX.clearScreen(out);
                    World.displayArea(player.location, player);
                    break;    
                case "describe":
                    if (tokens.length > 1){
                        player.description = "";
                        for(int i = 1; i < tokens.length; i++){
                            player.description += tokens[i] + " ";
                        }
                        World.writePlayer(player);
                    }
                    else
                        World.helpMe(out,tokens[0],true);
                    break;  
                case "save":
                    World.writePlayer(player);
                    GFX.clearScreen(out);
                    out.println("Player Saved...");
                    World.displayArea(player.location, player);
                    break; 
                case "help":
                    if (tokens.length > 1){
                        switch(tokens[1]){
                            case "look":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "walk":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "say":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "stats":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "describe":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "whisper":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "emote":
                                World.helpMe(out,tokens[1],false);
                                break OUTER;
                            case "take":
                            case "get":    
                                World.helpMe(out,tokens[1],false);
                                break OUTER;    
                            default:
                                World.helpMe(out,tokens[0],true);
                                break OUTER;                                
                        }
                    }
                    else{
                        World.helpMe(out,tokens[0],true);
                        break; 
                    }                             
                case "exit":
                case "quit":
                case "/q": 
                    World.writePlayer(player);
                    exitFlag = true;
                    break;
                default:
                    out.println("Command --[ " + command + "] -- is not valid.");
            }
        }
        return exitFlag;
    }
    
    private boolean playerLogin(Scanner in, PrintWriter out){
        boolean outcome = false;
        boolean exit = false;
        boolean validUsername;
        boolean validPassword;
        String playerName = null;
        String playerPassword = null;
        if(player.NPC){
            outcome = true;
            return outcome;
        }
        else
        {
            
            while(!exit){
                validUsername = false;
                validPassword = false;
                while(!validUsername){
                    setStage(in,out,true,false);
                    out.println("▼ USER @ The GRiD ");
                    out.flush();
                    playerName = in.nextLine();
                    if (World.isValidPlayername(playerName)){
                        validUsername = true;
                    } else {

                        setStage(in,out,true,false);
                        out.println("The player name " + playerName + " was not valid.");
                        out.println("Your player name must be at least three characters " +
                            "and only use letters, numbers, and the underscore.");
                        if (!doAgain(in, out)){
                            outcome = false;
                            exit = true;
                            break;
                        }
                    }
                }
                if (validUsername){
                    if (World.doesPlayerExist(playerName)){
                        while(!validPassword){
                            out.println("▼ PASSCODE @ The GRiD");
                            out.flush();
                            playerPassword = in.nextLine();
                            if (World.isValidPassword(playerPassword)){
                                    player.name = playerName;
                                    player.password = playerPassword;
                                    Player x = World.loadPlayer(playerName);
                                    String tmp = Integer.toHexString(playerPassword.hashCode());
                                    if(tmp.equals(x.password)){
                                        player.description = x.description;
                                        player.location = x.location;
                                        player.inventory.addAll(x.inventory);   
                                        validPassword = true;
                                        outcome = true;
                                        exit = true;
                                    }else{
                                        setStage(in,out,true,false);
                                        out.println("? PASSCODE [ERROR] ➟ ");
                                        if (!doAgain(in, out)){
                                            outcome = false;
                                            exit = true;
                                            break;
                                        }
                                    }                                                  
                            } else {
                                out.println("? PASSCODE [ERROR] ➟ ");
                                setStage(in,out,true,true);
                                if (!doAgain(in, out)){
                                    outcome = false;
                                    exit = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        out.println("       USER ➟ " + playerName + " [ERROR]");
                        if (doAgain(in, out, "CREATE USER ➟ " + playerName + "?")){
                            //setStage(in,out,true,false);
                            while(!validPassword){
                                String passwordRequirements = "A valid password is at least eight characters long, " +
                                    "contains at least one UPPERCASE letter, one lowercase letter, and one number.";
                                out.println(passwordRequirements);
                                out.print("SET PASSCODE ➟ ");
                                out.flush();
                                playerPassword = in.nextLine();
                                if (World.isValidPassword(playerPassword)){
                                    player.name = playerName;
                                    player.password = playerPassword;
                                    World.writePlayer(player);
                                    validPassword = true;
                                    outcome = true;
                                    exit = true;
                                } else {
                                    out.println("That password did not meet requirements.");
                                    if (!doAgain(in, out)){
                                        if (doAgain(in, out, "? ENTER NEW USER ➟ ")){
                                            exit = false;
                                        } else {
                                            outcome = false;
                                            exit = true;
                                        }
                                        break;
                                    }
                                }
                            }
                        } else {
                            if (doAgain(in, out, "? ENTER NEW USER ➟ ")){
                                exit = false;
                                validUsername = false;
                                validPassword = false;
                            } else {
                                outcome = false;
                                exit = true;
                            }
                        }
                    }
                }
            } 
            return outcome;
        }
    }
     
    private boolean doAgain(Scanner in, PrintWriter out){
        String defaultPrompt = "? ATTEMPT TO RECONnect ➟ ";
        return doAgain(in, out, defaultPrompt);
    }
    
    private boolean doAgain(Scanner in, PrintWriter out, String prompt){
        String choice;
        boolean valid = false;
        boolean outcome = true;
        while(!valid){
            out.print(prompt + " Y||N ");
            out.flush();
            choice = in.nextLine().toLowerCase();
            if (choice.equals("yes") || choice.equals("y")){
                outcome = true;
                valid = true;
            } else if (choice.equals("no") || choice.equals("n")){
                outcome = false;
                valid = true;
            }
        }
        return outcome;
    }
    
    private void setStage(Scanner in,PrintWriter out,boolean main,boolean wait){
        if(wait){
            String playerIn;
            playerIn = in.nextLine();  
        }else{
            GFX.clearScreen(out);
        if(main){
            out.println(String.format("|%67s|",GFX.mmBorder));
            for(String x: GFX.theGRID){
                out.println(String.format("|   %-10s   |", x));
            }
            out.println(String.format("|%69s|", "Designed by InvIcTuS \\,,/(O.\\\\)\\,,/   "));
            out.println(String.format("|%67s|",GFX.mmBorder));
        }
        else
            World.displayArea(player.location, player);
        }
    }
}