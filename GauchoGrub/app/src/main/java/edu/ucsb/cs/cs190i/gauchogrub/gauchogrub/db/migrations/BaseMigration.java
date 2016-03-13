package edu.ucsb.cs.cs190i.gauchogrub.gauchogrub.db.migrations;

import java.util.logging.Logger;

import io.requery.BlockingEntityStore;
import io.requery.Persistable;
import io.requery.sql.EntityDataStore;
import io.requery.sql.SchemaModifier;

public abstract class BaseMigration {

    protected final static Logger logger = Logger.getLogger("Migrations");

    protected SchemaModifier schemaModifier;
    // Blocking because we want a migration to run in real-time (synchronously)
    protected BlockingEntityStore<Persistable> dataStore;

    public BaseMigration(SchemaModifier schemaModifier, EntityDataStore<Persistable> dataStore) {
        this.schemaModifier = schemaModifier;
        this.dataStore = dataStore.toBlocking();
    }

    public abstract void forwards();
    public abstract void backwards();
}
