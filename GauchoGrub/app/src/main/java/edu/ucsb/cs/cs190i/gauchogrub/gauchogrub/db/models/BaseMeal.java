package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;


import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

/* Meal type: breakfast, dinner, lunch, brunch, etc. */
@Entity
public class BaseMeal {

    @Key
    @Generated
    public int id;

    @Column(nullable = false, index = true, unique = true)
    public String name;

}
