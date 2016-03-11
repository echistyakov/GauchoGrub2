package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.LocalDate;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters.LocalDateConverter;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.query.Result;

@Entity
public class Menu {

    @Key
    @Generated
    int id;

    @ForeignKey
    @Column(nullable = false, index = true, unique = false)
    RepeatedEvent event;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalDateConverter.class)
    LocalDate date;

    @JunctionTable(name = "menu_to_menuitem")
    @ManyToMany(mappedBy = "menus")
    Result<MenuItem> menuItems;

}
