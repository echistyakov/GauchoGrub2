package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.LocalTime;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters.LocalTimeConverter;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;

/* An event repeated every week on a specific day in a specific DiningCommon during a specific Meal.
 * Has a start and end time. */
@Entity
public class RepeatedEvent {

    @Key
    @Generated
    int id;

    @ManyToOne
    @Column(nullable = false, index = true, unique = false)
    DiningCommon diningCommon;

    @ManyToOne
    @Column(nullable = false, index = true, unique = false)
    Meal meal;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    LocalTime startTime;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    LocalTime endTime;

    @Column(nullable = false, index = true, unique = false)
    /* Use day-of-the-week values from Joda DateTimeConstants class (1-indexed) */
    int dayOfWeek;

}