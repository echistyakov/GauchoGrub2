package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.converters;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import io.requery.Converter;

/* Converter for Joda DateTime */
public class LocalDateConverter implements Converter<LocalDate, Long> {

    @Override
    public Class<LocalDate> mappedType() {
        return LocalDate.class;
    }

    @Override
    public Class<Long> persistedType() {
        return Long.class;
    }

    @Override
    public Integer persistedSize() {
        return null;
    }

    @Override
    public Long convertToPersisted(LocalDate value) {
        return (value == null) ? null : value.toDateTime(LocalTime.MIDNIGHT).getMillis();
    }

    @Override
    public LocalDate convertToMapped(Class<? extends LocalDate> type, Long value) {
        return (value == null) ? null : new LocalDate(value);
    }
}
