package mclamud;


import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;


public class PlayerThread implements Runnable {
    
    private final Socket psock;
    private final Player player;
    private final Stooges GMAN = new Stooges();
    private final Stooges GMAN2 = new Stooges();
    private final Stooges GMAN3 = new Stooges();
    private final int[] filesForNPC = {141,142,143,144,145,146,147,148,149,1412,1413,1452,1453,1462,1463};
    private static int cmdCtr = 0;
    
    
    
    public PlayerThread(Socket sock){
        this.psock= sock;
        this.player = new Player(this.psock);
    }

    @Override
    public void run() {  
        Scanner in;
        PrintWriter out;
        String playerIn;
        boolean exit = false;
        boolean shutUp = false;
        GMAN.name ="Moe";
        GMAN2.name ="Larry";
        GMAN3.name ="Curly";
        try {
            in = new Scanner(psock.getInputStream());
            out = new PrintWriter(psock.getOutputStream(), true);
            out.println("Welcome to the North Adamselot! Mind th e gaps in reali ty");
            World.movePlayer(GMAN, filesForNPC[(int)(Math.random()*filesForNPC.length)]);
            World.movePlayer(GMAN2,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
            World.movePlayer(GMAN3,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
            if (!playerLogin(in, out)){ // Failed login
                exit = true;
                exitPlayer();
            } else { //Successful login.
                World.movePlayer(player, player.location);
                World.sendMessageToArea(player, player.name + " has arrived.");
                GFX.clearScreen(out);
                World.displayArea(player.location, player);
            }
            while(!exit){ //Main loop
                if(player.location == GMAN.location && shutUp==false){
                    World.sendMessageToPlayer(GMAN, player.name, "Have you seen Larry or Curly?");
                    shutUp = true;
                }
                else if(player.location == GMAN2.location && shutUp==false){
                    World.sendMessageToPlayer(GMAN2, player.name, "Have you seen Moe or Curly?");
                    shutUp = true;
                }
                else if(player.location == GMAN3.location && shutUp==false){
                    World.sendMessageToPlayer(GMAN3, player.name, "Have you seen Moe or Larry?");
                    shutUp = true;
                }
                playerIn = in.nextLine();
                String[] tmp = playerIn.split(" ", 2);
                // IF PLAYER MOVES NPC MOVES
                if(tmp[0].equals("walk") || tmp[0].equals("w") || tmp[0].equals("go") || tmp[0].equals("g")){
                    World.movePlayer(GMAN, filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    World.movePlayer(GMAN2,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    World.movePlayer(GMAN3,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    shutUp = false;
                }
                cmdCtr++;
                // MOVE EVERY 4 COMMANDS
                if(cmdCtr == 4){
                    World.movePlayer(GMAN, filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    World.movePlayer(GMAN2,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    World.movePlayer(GMAN3,filesForNPC[(int)(Math.random()*filesForNPC.length)]);
                    cmdCtr = 0;
                }
                    
                exit = commandDispatcher(playerIn, out);
            }       
            
            //Player has left world
            World.sendMessageToArea(player, player.name + " has returned to reality.");
            exitPlayer();
                
        } catch (IOException | NoSuchElementException e) {
            //Connection has been lost
            World.sendMessageToArea(player, player.name + " crumbles into dust.");
            exitPlayer();
        }
    }
    
    private void exitPlayer(){
        try {
            String ip = psock.getInetAddress().toString();
            System.out.println(ip + " disconnected.");
            psock.close();
        } catch (IOException ex){ 
            System.out.println("An IOException occurred when a player exited.");
        }
        finally{
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
                case "fight":
                    World.doBattle(player,punchingBag);
                    break;    
                case "stats":
                case "/s":
                    switch(tokens.length){
                        case 1:
                            World.showPlayer(out, player);
                            break OUTER;
                        case 2:
                            World.showPlayer(out, player, tokens[1]);
                            break OUTER;
                        default:
                            World.helpMe(out, tokens[0], true);
                            break OUTER;
                    }
                case "npc":
                case "/n":
                    if(tokens.length == 3){
                        World.npcResponse(out, player, tokens[1], tokens[2]);
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
                            GFX.clearScreen(out);
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
                    else
                        World.helpMe(out,tokens[0],true);
                    break;    
                case "exit":
                case "quit":
                case "/q": 
                    World.writePlayer(player);
                    exitFlag = true;
                    break;
                default:
                    out.println("Command \"" + command + "\" is not valid.");
            }
        }
        return exitFlag;
    }
    
    private boolean playerLogin(Scanner in, PrintWriter out){
        boolean outcome = false;
        boolean exit = false;
        boolean validUsername;
        boolean validPassword;
        boolean validClass;
        String playerName = null;
        String playerPassword = null;
        while(!exit){
            validUsername = false;
            validPassword = false;
            validClass = false;
            while(!validUsername){
                out.print("Enter your existing or desired player name: ");
                out.flush();
                playerName = in.nextLine();
                if (World.isValidPlayername(playerName)){
                    validUsername = true;
                } else {
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
                        out.print("Enter your password: ");
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
                            }
                            else{
                               out.println("The password did not match our records.");
                                if (!doAgain(in, out)){
                                    outcome = false;
                                    exit = true;
                                    break;
                            } 
                            }
                        } else {
                            out.println("The password was not correct.");
                            if (!doAgain(in, out)){
                                outcome = false;
                                exit = true;
                                break;
                            }
                        }
                    }
                } else {
                    out.println("The player name " + playerName + " does not exist.");
                    if (doAgain(in, out, "Would you like to use the name " + playerName + "?")){
                        while(!validPassword){
                            String passwordRequirements = "A valid password is at least eight characters long, " +
                                "contains at least one UPPERCASE letter, one lowercase letter, and one number.";
                            out.println(passwordRequirements);
                            out.print("Enter a password: ");
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
                                    if (doAgain(in, out, "Would you like to enter a new player name?")){
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
                        if (doAgain(in, out, "Would you like to enter a new player name?")){
                            exit = false;
                        } else {
                            outcome = false;
                            exit = true;
                        }
                    }
                }
            }
            GFX.clearScreen(out);
            String z = "";
            while(!validClass){
                out.println("What hat shall you don Today?");
                out.println("1: Student");
                z = in.nextLine();
                if(z.equals("1")){
                    Student X = new Student();
                    player.setAC(X.getAC());
                    player.setHP(X.getHP());
                    validClass = true;
                }
                else
                    GFX.clearScreen(out);
//                System.out.println(player.getAC());
//                System.out.println(player.getHP());
            }
            
        }
        return outcome;
    }
     
    private boolean doAgain(Scanner in, PrintWriter out){
        String defaultPrompt = "Do you want to try again?";
        return doAgain(in, out, defaultPrompt);
    }
    
    private boolean doAgain(Scanner in, PrintWriter out, String prompt){
        String choice;
        boolean valid = false;
        boolean outcome = true;
        while(!valid){
            out.print(prompt + " Yes or no? ");
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
    
}