package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.LocalTime;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters.LocalTimeConverter;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;

@Entity
public class RepeatedEvent {

    @Key
    @Generated
    int id;

    @ForeignKey
    @Column(nullable = false, index = true, unique = false)
    DiningCommon diningCommon;

    @ForeignKey
    @Column(nullable = false, index = true, unique = false)
    Meal meal;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    LocalTime from;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    LocalTime to;

    @Column(nullable = false, index = true, unique = false)
    /* Use day-of-the-week values from Joda DateTimeConstants class (1-indexed) */
    int dayOfWeek;

}