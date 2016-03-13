package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.LocalDate;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters.LocalDateConverter;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.JunctionTable;
import io.requery.Key;
import io.requery.ManyToMany;
import io.requery.ManyToOne;
import io.requery.query.Result;

/* Menu (i.e. a list of MenuItems served during a specific RepeatedEvent on a specific date. */
@Entity
public class Menu {

    @Key
    @Generated
    public int id;

    @ManyToOne
    @Column(nullable = false, index = true, unique = false)
    public RepeatedEvent event;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalDateConverter.class)
    public LocalDate date;

    @JunctionTable
    @ManyToMany(mappedBy = "menus")
    public Result<MenuItem> menuItems;

}
