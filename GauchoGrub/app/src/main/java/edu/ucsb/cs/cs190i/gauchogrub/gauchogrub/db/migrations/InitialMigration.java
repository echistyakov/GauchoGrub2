package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.migrations;

import android.content.Context;

import com.opencsv.CSVReader;

import org.joda.time.LocalTime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.R;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommon;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.DiningCommonEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.MealEntity;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEventEntity;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;

public class InitialMigration extends BaseMigration {

    Context context;

    public InitialMigration(EntityDataStore<Persistable> dataStore, Context context) {
        super(dataStore);
        this.context = context;
    }

    @Override
    public void forwards() {
        try {
            prepopulateDiningCommons();
            prepopulateMeals();
            prepopulateRepeatedEvents();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception thrown during Initial migration.", e);
        }
    }

    @Override
    public void backwards() {
        // Call to value() actually evaluates the query
        this.dataStore.delete(DiningCommonEntity.class).get().value();
        this.dataStore.delete(MealEntity.class).get().value();
        this.dataStore.delete(RepeatedEventEntity.class).get().value();
    }

    private void prepopulateDiningCommons() throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.dining_commons);
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)));
        // Row: | DiningCommonName |
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            String dcName = row[0];
            DiningCommonEntity entity = new DiningCommonEntity();
            entity.setName(dcName);
            this.dataStore.insert(entity);
        }
    }

    private void prepopulateMeals() throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.meals);
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)));
        // Row: | MealName |
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            String mealName = row[0];
            MealEntity entity = new MealEntity();
            entity.setName(mealName);
            this.dataStore.insert(entity);
        }
    }

    private void prepopulateRepeatedEvents() throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.repeated_events);
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)));
        // Row: | DiningCommonName | MealName | StartTime | EndTime | DayOfWeek |
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            // Get dining common id
            String dcName = row[0];
            DiningCommon dc = this.dataStore.select(DiningCommonEntity.class).where(DiningCommonEntity.NAME.equal(dcName)).get().first();
            // Get meal id
            String mealName = row[1];
            Meal meal = this.dataStore.select(MealEntity.class).where(MealEntity.NAME.equal(mealName)).get().first();
            // From/to time
            LocalTime startTime = LocalTime.parse(row[2]);
            LocalTime endTime = LocalTime.parse(row[3]);
            int dayOfWeek = Integer.parseInt(row[4]);

            RepeatedEventEntity entity = new RepeatedEventEntity();
            entity.setDiningCommon(dc);
            entity.setMeal(meal);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setDayOfWeek(dayOfWeek);
            this.dataStore.insert(entity);
        }
    }
}
