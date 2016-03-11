package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import io.requery.Column;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.query.MutableResult;

@Entity
public class MenuItem {

    @Key
    @Generated
    int id;

    @ForeignKey
    @Column(nullable = false, index = true, unique = false)
    MenuCategory menuCategory;

    @Column(nullable = false, index = true, unique = false)
    String title;

    @ManyToMany(mappedBy = "menuItems")
    MutableResult<Menu> menus;

    @Column(nullable = false, index = true, unique = false, value = "false")
    boolean isVegetarian;

    @Column(nullable = false, index = true, unique = false, value = "false")
    boolean isVegan;

    @Column(nullable = false, index = true, unique = false, value = "false")
    boolean hasNuts;

}
