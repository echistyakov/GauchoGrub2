package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;

@Entity
public class DiningCommon {

    @Key
    @Generated
    int id;

    @Column(nullable = false, index = true, unique = true)
    String name;

}