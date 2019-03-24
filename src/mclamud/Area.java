package mclamud;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Area {
    public String title = "";
    public String gfx = "";
    public ArrayList<String> items = new ArrayList<>();
    public int[] exits = {0,0,0,0,0,0};
    public Map<String, Player> players = new HashMap<>();
    public String description = "";
    String description2 = "";
}