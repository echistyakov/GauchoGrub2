package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models;

import org.joda.time.LocalTime;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters.LocalTimeConverter;
import io.requery.Column;
import io.requery.Convert;
import io.requery.Entity;
import io.requery.ForeignKey;
import io.requery.Generated;
import io.requery.Key;
import io.requery.ManyToOne;
import io.requery.OneToMany;
import io.requery.query.Result;

/* An event repeated every week on a specific day in a specific DiningCommon during a specific Meal.
 * Has a start and end time. */
@Entity
public class BaseRepeatedEvent {

    @Key
    @Generated
    public int id;

    @ForeignKey(references = BaseDiningCommon.class)
    @Column(nullable = false, index = true, unique = false)
    public int diningCommonId;

    //@ManyToOne
    //public BaseDiningCommon diningCommon;

    @ForeignKey(references = BaseMeal.class)
    @Column(nullable = false, index = true, unique = false)
    public int mealId;

    //@ManyToOne
    //public BaseMeal meal;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    public LocalTime startTime;

    @Column(nullable = false, index = true, unique = false)
    @Convert(LocalTimeConverter.class)
    public LocalTime endTime;

    @Column(nullable = false, index = true, unique = false)
    /* Use day-of-the-week values from Joda DateTimeConstants class (1-indexed) */
    public int dayOfWeek;

    @OneToMany
    public Result<Menu> menus;

}