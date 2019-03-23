package mclamud;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final int port = 6666;
    public static ArrayList<Player> curUsers = new ArrayList<>();
    private static Socket npc;
    private static Socket npc2;
    private static final Runnable NPC = new PlayerThread(npc,"NPC"); 
    private static final Runnable NPC2 = new PlayerThread(npc2,"NPC2"); 
    public static void main(String[] args) throws IOException {
        int ctr = 0;
        final int PORT = 6666;
        final int THREADS = 500;
        boolean exit = false;
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        ExecutorService incubator = Executors.newFixedThreadPool(20);
        incubator.execute(NPC);
        incubator.execute(NPC2);
        
        try {
            
            for(String x: GFX.theGRID){
                System.out.println(x);
            }
            System.out.println("GRID MUD 1.0");
            System.out.println("The GRID is recieving injections on port " + PORT);
            System.out.println("NPC  created ┌∩┐(◣_◢)┌∩┐");
            System.out.println("NPC2 created ┌∩┐(◣_◢)┌∩┐");
            ServerSocket ssock = new ServerSocket(PORT);

            
            
            while(!exit){
                Socket psock; 
                psock = ssock.accept();
                Runnable thread = new PlayerThread(psock,"");
                pool.execute(thread);
                String client = psock.getInetAddress().toString(); 
                ctr++;
                if(ctr%2==0)
                    System.out.println(client + " ]------ tapped on link ---►  " + psock.getPort() + "   ┌∩┐(◣_◢)┌∩┐");
                else if(ctr%3==0)
                    System.out.println(client + " ]------ tapped on link ---►  " + psock.getPort() + "  ¯̿̿¯̿̿'̿̿)̿̿̿ '̿̿̿̿̿̿\\̵͇̿̿\\=(•̪̀●́)=o/̵͇̿̿/'̿̿ ̿ ̿̿");
                else
                    System.out.println(client + " ]------ tapped on link ---►  " + psock.getPort() + "   ̿' ̿'\\̵͇̿̿\\з=(◕_◕)=ε/̵͇̿̿/'̿'̿ ̿");
                if(ssock.isBound() && curUsers.size()> 0){
                    System.out.print(" Current Users -> ");
                    curUsers.forEach((x) -> {
                        System.out.print(x.name);
                    });
                    System.out.println();
                }else if(ssock.isBound() && curUsers.isEmpty()){
                    System.out.print(" Current Users -> ");
                }
             }
        } catch (IOException ex){
            System.out.println("Something went wrong.");
        }
        pool.shutdownNow();
        incubator.shutdownNow();
    }

    public Server() throws IOException {
        Server.npc = new Socket("127.0.0.5",port);
        Server.npc2 = new Socket("127.0.0.6",port);
    }

}
