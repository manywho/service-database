package com.manywho.services.manywho.database;

import com.manywho.services.manywho.database.factories.RethinkDBConnectionFactory;
import com.manywho.services.manywho.database.factories.RethinkDBFactory;
import com.manywho.services.manywho.database.managers.DatabaseManager;
import com.manywho.services.manywho.database.managers.DescribeManager;
import com.manywho.services.manywho.database.services.DatabaseLoadService;
import com.manywho.services.manywho.database.services.DatabaseSaveService;
import com.manywho.services.manywho.database.services.DatabaseService;
import com.manywho.services.manywho.database.services.DescribeService;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.net.Connection;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

class ApplicationBinder extends AbstractBinder {
    @Override
    protected void configure() {
        bindFactory(RethinkDBConnectionFactory.class).to(Connection.class);
        bindFactory(RethinkDBFactory.class).to(RethinkDB.class);

        bind(DatabaseManager.class).to(DatabaseManager.class);
        bind(DatabaseService.class).to(DatabaseService.class);
        bind(DatabaseLoadService.class).to(DatabaseLoadService.class);
        bind(DatabaseSaveService.class).to(DatabaseSaveService.class);

        bind(DescribeManager.class).to(DescribeManager.class);
        bind(DescribeService.class).to(DescribeService.class);
    }
}
