package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub;

import android.app.Application;
import android.content.SharedPreferences;

import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.migrations.InitialMigration;
import edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.models.Models;

import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;

public class GGApp extends Application {

    private EntityDataStore<Persistable> dataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        String packageName = getApplicationContext().getPackageName();
        SharedPreferences prefs = getSharedPreferences(packageName, MODE_PRIVATE);

        // If this is the first time an app is launched
        if (prefs.getBoolean("first_run", true)) {
            InitialMigration migration = new InitialMigration(this.getData(), getApplicationContext());
            // Clear our tables in case `first_run` flag was somehow compromised
            migration.backwards();
            // Prepopulate tables with initial data
            migration.forwards();
            // Set `first_run` flag to false
            prefs.edit().putBoolean("first_run", false).apply();
        }
    }

    /**
     * @return {@link EntityDataStore} single instance for the application.
     * <p/>
     * Note if you're using Dagger you can make this part of your application level module returning
     * {@code @Provides @Singleton}.
     */
    public EntityDataStore<Persistable> getData() {
        if (dataStore == null) {
            // override onUpgrade to handle migrating to a new version
            DatabaseSource source = new DatabaseSource(this, Models.DEFAULT, 1);
            Configuration configuration = source.getConfiguration();
            dataStore = new EntityDataStore<>(configuration);
        }
        return dataStore;
    }
}
