package com.manywho.services.manywho.database.services;

import com.manywho.sdk.entities.draw.elements.type.TypeElementProperty;
import com.manywho.sdk.entities.draw.elements.type.TypeElementPropertyCollection;
import com.manywho.sdk.enums.ContentType;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Db;
import com.rethinkdb.net.Connection;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescribeService {
    @Inject
    private RethinkDB rethinkDB;

    @Inject
    private Connection<?> connection;

    @Inject
    private DatabaseService databaseService;

    public void createTenantDatabase(String tenant) {
        Map<String, String> result = rethinkDB.dbCreate(tenant)
                .run(connection);

        // TODO: Check result to see if database was actually created
    }

    public boolean doesTenantDatabaseExist(String tenant) {
        return rethinkDB.dbList().contains(tenant).run(connection);
    }

    public List<String> listTenantTables(String tenant) {
        return rethinkDB.db(tenant).tableList()
                .filter(t -> t.eq("__Schemas").not())
                .run(connection);
    }

    public boolean doesTenantTableExist(String tenant, String tableName) {
        return rethinkDB.db(tenant).tableList().contains(tableName).run(connection);
    }

    public void createTenantTable(String tenant, String tableName) {
        rethinkDB.db(tenant).tableCreate(tableName).run(connection);
    }

    public void createTenantTableSchema(String tenant, String tableName, List<TypeElementProperty> properties) {
        // Create the reserved __Schemas table if it doesn't exist
        if (!doesTenantTableExist(tenant, "__Schemas")) {
            createTenantTable(tenant, "__Schemas");
        }

        Map<String, String> fields = new HashMap<>();

        for (TypeElementProperty property : properties) {
            fields.put(property.getDeveloperName(), property.getContentType().toString());
        }

        Map<String, Object> schema = new HashMap<>();
        schema.put("name", tableName);
        schema.put("fields", fields);

        rethinkDB.db(tenant).table("__Schemas").insert(schema).run(connection);
    }

    public void updateTenantTableSchema(String tenant, String tableName, List<TypeElementProperty> properties) throws Exception {
        Db database = rethinkDB.db(tenant);

        // Fetch the current schema
        Map<String, Object> currentSchema = databaseService.loadTenantSchema(database, tableName);
        if (!currentSchema.containsKey("fields")) {
            currentSchema.put("fields", new HashMap<String, ContentType>());
        }

        Map<String, String> fields = (Map<String, String>) currentSchema.get("fields");
        for (TypeElementProperty property : properties) {
            fields.put(property.getDeveloperName(), property.getContentType().toString());
        }

        database.table("__Schemas").update(currentSchema).run(connection);
    }
}
