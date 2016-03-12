package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;

/* A favorite MenuItem (note: specific to a DiningCommons) */
@Entity
public class Favorite {

    @Key
    @Generated
    int id;

    @ForeignKey
    @Column(nullable = false, index = true, unique = true)
    DiningCommon diningCommon;

    @ForeignKey
    @Column(nullable = false, index = true, unique = true)
    MenuItem menuItem;

}
