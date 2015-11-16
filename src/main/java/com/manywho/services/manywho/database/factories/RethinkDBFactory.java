package com.manywho.services.manywho.database.factories;

import com.rethinkdb.RethinkDB;
import org.glassfish.hk2.api.Factory;

public class RethinkDBFactory implements Factory<RethinkDB> {
    @Override
    public RethinkDB provide() {
        return RethinkDB.r;
    }

    @Override
    public void dispose(RethinkDB rethinkDB) {

    }
}
