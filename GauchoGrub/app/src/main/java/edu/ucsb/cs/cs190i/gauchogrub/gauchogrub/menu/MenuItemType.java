package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.menu;

import java.util.HashMap;

public class MenuItemType extends NamedEnum {
    public String getShortVersion() {
        HashMap<String, String> map = new HashMap<>();
        map.put("Vegetarian", "(v)");
        map.put("Vegan", "(vgn)");
        map.put("Regular", "");

        return (map.containsKey(this.name)) ? map.get(this.name) : "";
    }
}
