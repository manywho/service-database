package com.manywho.services.manywho.database.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manywho.services.manywho.database.entities.Schema;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Db;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import javax.inject.Inject;
import java.util.Map;

public class DatabaseService {
    @Inject
    private RethinkDB rethinkDB;

    @Inject
    private Connection<?> connection;

    @Inject
    private ObjectMapper objectMapper;

    public Db loadTenantDatabase(String databaseName) {
        return rethinkDB.db(databaseName);
    }

    public Table loadTenantTable(Db database, String tableName) {
        return database.table(tableName);
    }

    public Schema loadTenantSchemaAsPojo(Db database, String tableName) throws Exception {
        return objectMapper.convertValue(loadTenantSchema(database, tableName), Schema.class);
    }

    public Map<String, Object> loadTenantSchema(Db database, String tableName) throws Exception {
        Cursor<Map<String, Object>> cursor = database.table("__Schemas")
                .filter(s -> s.getField("name").eq(tableName))
                .limit(1)
                .run(connection);

        if (cursor.hasNext()) {
            return cursor.next();
        }

        throw new Exception("A schema could not be found for the type " + tableName);
    }
}
