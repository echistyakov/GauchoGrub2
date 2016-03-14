package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.migrations;

import android.content.Context;
import android.util.Log;

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
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Meal;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.RepeatedEvent;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;
import io.requery.sql.TableCreationMode;

public class InitialMigration extends BaseMigration {

    Context context;

    public InitialMigration(SchemaModifier schemaModifier, EntityDataStore<Persistable> dataStore, Context context) {
        super(schemaModifier, dataStore);
        this.context = context;
    }

    @Override
    public void forwards() {
        this.schemaModifier.createTables(TableCreationMode.DROP_CREATE);
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
        this.schemaModifier.dropTables();
    }

    private void prepopulateDiningCommons() throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.dining_commons);
        CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(inputStream)));
        // Row: | DiningCommonName |
        List<String[]> rows = reader.readAll();
        for (String[] row : rows) {
            String dcName = row[0];
            DiningCommon entity = new DiningCommon();
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
            Meal entity = new Meal();
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
            DiningCommon dc = this.dataStore.select(DiningCommon.class).where(DiningCommon.NAME.equal(dcName)).get().first();
            // Get meal id
            String mealName = row[1];
            Meal meal = this.dataStore.select(Meal.class).where(Meal.NAME.equal(mealName)).get().first();
            // From/to time
            LocalTime startTime = LocalTime.parse(row[2]);
            LocalTime endTime = LocalTime.parse(row[3]);
            int dayOfWeek = Integer.parseInt(row[4]);

            RepeatedEvent entity = new RepeatedEvent();
            entity.setDiningCommonId(dc.getId());
            entity.setMealId(meal.getId());
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setDayOfWeek(dayOfWeek);
            this.dataStore.insert(entity);
        }
    }
}
