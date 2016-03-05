package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.menu;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class Menu {
    @SerializedName("Date")
    public DateTime date;
    @SerializedName("Event")
    public RepeatedEvent event;
    @SerializedName("MenuItems")
    public ArrayList<MenuItem> menuItems;
    @SerializedName("Id")
    public int menuID;
}