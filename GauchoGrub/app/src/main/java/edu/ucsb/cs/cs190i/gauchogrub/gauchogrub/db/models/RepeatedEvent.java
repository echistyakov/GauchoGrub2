package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.Interval;

import io.requery.Column;
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
    Interval from;

    @Column(nullable = false, index = true, unique = false)
    Interval to;

    @Column(nullable = false, index = true, unique = false)
    int dayOfWeek;

}
