package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters;

import org.joda.time.LocalTime;

import io.requery.Converter;

/* Converter for Joda LocalTime */
public class LocalTimeConverter implements Converter<LocalTime, Integer> {

    @Override
    public Class<LocalTime> mappedType() {
        return LocalTime.class;
    }

    @Override
    public Class<Integer> persistedType() {
        return Integer.class;
    }

    @Override
    public Integer persistedSize() {
        return null;
    }

    @Override
    public Integer convertToPersisted(LocalTime value) {
        return (value == null) ? null : value.getMillisOfDay();
    }

    @Override
    public LocalTime convertToMapped(Class<? extends LocalTime> type, Integer value) {
        return (value == null) ? null : LocalTime.fromMillisOfDay(value);
    }
}
