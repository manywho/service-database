package com.manywho.services.manywho.database.factories;

import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import org.glassfish.hk2.api.Factory;

import javax.inject.Inject;
import java.util.concurrent.TimeoutException;

public class RethinkDBConnectionFactory implements Factory<Connection> {
    @Inject
    private RethinkDB rethinkDB;

    @Override
    public Connection provide() {
        try {
            return rethinkDB.connection()
                    .hostname("localhost")
                    .connect();
        } catch (TimeoutException e) {
            e.printStackTrace();

            return null;
        }
    }

    @Override
    public void dispose(Connection connection) {

    }
}
