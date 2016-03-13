package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

/* A favorite MenuItem (note: specific to a DiningCommons) */
@Entity
public class Favorite {

    @Key
    @Generated
    public int id;

    @ManyToOne
    @Column(nullable = false, index = true, unique = true)
    public DiningCommon diningCommon;

    @ManyToOne
    @Column(nullable = false, index = true, unique = true)
    public MenuItem menuItem;

}
