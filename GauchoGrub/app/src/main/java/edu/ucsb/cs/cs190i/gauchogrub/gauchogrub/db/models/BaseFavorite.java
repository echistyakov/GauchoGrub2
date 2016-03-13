package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

/* A favorite MenuItem (note: specific to a DiningCommons) */
@Entity
public class BaseFavorite {

    @Key
    @Generated
    public int id;

    @ForeignKey(references = BaseDiningCommon.class)
    @Column(nullable = false, index = true, unique = false)
    public int diningCommonId;

    @ForeignKey(references = BaseMenuItem.class)
    @Column(nullable = false, index = true, unique = false)
    public int menuItemId;

    @ManyToOne
    public BaseDiningCommon diningCommon;

    @ManyToOne
    public BaseMenuItem menuItem;

}
