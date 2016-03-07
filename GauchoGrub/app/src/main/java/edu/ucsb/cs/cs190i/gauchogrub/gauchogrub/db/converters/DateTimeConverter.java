package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters;

import org.joda.time.DateTime;

import io.requery.Converter;

public class DateTimeConverter implements Converter<DateTime, Long> {

    // TODO: converter for Joda DateTime

    @Override
    public Class<DateTime> mappedType() {
        return null;
    }

    @Override
    public Class<Long> persistedType() {
        return null;
    }

    @Override
    public Integer persistedSize() {
        return null;
    }

    @Override
    public Long convertToPersisted(DateTime value) {
        return null;
    }

    @Override
    public DateTime convertToMapped(Class<? extends DateTime> type, Long value) {
        return null;
    }
}
