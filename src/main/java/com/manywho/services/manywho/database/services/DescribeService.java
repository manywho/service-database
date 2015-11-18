package com.manywho.services.manywho.database.services;

import com.manywho.sdk.entities.draw.elements.type.*;
import com.manywho.sdk.entities.security.AuthenticatedWho;
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

    public boolean doesTenantTableExist(String tenant, String tableName) throws Exception {
        tableName = generateSafeName(tableName);

        return rethinkDB.db(tenant).tableList().contains(tableName).run(connection);
    }

    public void createTenantTable(String tenant, String tableName) throws Exception {
        tableName = generateSafeName(tableName);

        rethinkDB.db(tenant).tableCreate(tableName).run(connection);
    }

    public void createTenantTableSchema(String tenant, String tableName, List<TypeElementPropertyBinding> properties) throws Exception {
        // Create the reserved __Schemas table if it doesn't exist
        if (!doesTenantTableExist(tenant, "__Schemas")) {
            createTenantTable(tenant, "__Schemas");
        }

        Map<String, String> fields = new HashMap<>();

        for (TypeElementPropertyBinding property : properties) {
            fields.put(generateSafeName(property.getDatabaseFieldName()), property.getDatabaseContentType());
        }

        Map<String, Object> schema = new HashMap<>();
        schema.put("name", tableName);
        schema.put("fields", fields);

        rethinkDB.db(tenant).table("__Schemas").insert(schema).run(connection);
    }

    public void updateTenantTableSchema(String tenant, String tableName, List<TypeElementPropertyBinding> properties) throws Exception {
        Db database = rethinkDB.db(tenant);

        // Fetch the current schema
        Map<String, Object> currentSchema = databaseService.loadTenantSchema(database, tableName);
        if (!currentSchema.containsKey("fields")) {
            currentSchema.put("fields", new HashMap<String, ContentType>());
        }

        Map<String, String> fields = (Map<String, String>) currentSchema.get("fields");
        for (TypeElementPropertyBinding property : properties) {
            fields.put(generateSafeName(property.getDatabaseFieldName()), property.getDatabaseContentType());
        }

        database.table("__Schemas").update(currentSchema).run(connection);
    }

    public String getDatabaseName(AuthenticatedWho authenticatedWho) throws Exception {
        if (authenticatedWho == null) {
            throw new Exception(("The AuthenticatedWho object cannot be null."));
        }

        if (authenticatedWho.getManyWhoTenantId() == null ||
                authenticatedWho.getManyWhoTenantId().isEmpty() == true) {
            throw new Exception("The AuthenticatedWho.ManyWhoTenantId cannot be null or blank.");
        }

        return generateSafeName(authenticatedWho.getManyWhoTenantId());
    }

    public TypeElementBinding generateBinding(TypeElement typeElement) throws Exception {
        if (typeElement == null) {
            throw new Exception("The TypeElement object cannot be null.");
        }

        if (typeElement.getDeveloperName() == null ||
                typeElement.getDeveloperName().isEmpty() == true) {
            throw new Exception("The TypeElement.DeveloperName property cannot be null or blank when generating a binding.");
        }

        if (typeElement.getProperties() == null ||
                typeElement.getProperties().size() == 0) {
            throw new Exception("The TypeElement.Properties property cannot be null or empty. You cannot generate a binding if you don't have any properties.");
        }

        if (typeElement.getServiceElementId() == null ||
                typeElement.getServiceElementId().isEmpty() == true) {
            throw new Exception("The TypeElement.ServiceElementId property cannot be null or blank. This is needed so the service can bind fully to itself.");
        }

        TypeElementBinding typeElementBinding = new TypeElementBinding();
        typeElementBinding.setDeveloperName(typeElement.getDeveloperName() + " Binding");
        typeElementBinding.setDeveloperSummary("The automatic binding created for " + typeElement.getDeveloperName());
        typeElementBinding.setDatabaseTableName(generateSafeName(typeElement.getDeveloperName()));
        typeElementBinding.setPropertyBindings(new TypeElementPropertyBindingCollection());
        typeElementBinding.setServiceElementId(typeElement.getServiceElementId());

        // Convert each of the properties over to bindings
        for (TypeElementProperty typeElementProperty : typeElement.getProperties()) {
            TypeElementPropertyBinding typeElementPropertyBinding = new TypeElementPropertyBinding();
            typeElementPropertyBinding.setTypeElementPropertyId(typeElementProperty.getId());
            typeElementPropertyBinding.setDatabaseContentType(typeElementProperty.getContentType().toString());
            typeElementPropertyBinding.setDatabaseFieldName(generateSafeName(typeElementProperty.getDeveloperName()));

            typeElementBinding.getPropertyBindings().add(typeElementPropertyBinding);
        }

        return typeElementBinding;
    }

    private String generateSafeName(String name) throws Exception {
        if (name == null || name.isEmpty() == true) {
            throw new Exception("The name cannot be made safe as it is null or empty.");
        }

        // Replace all funny or blank characters with an underscore
        name = name.replaceAll("[^A-Za-z0-9]", "_");

        // Make all of the characters lower case
        name = name.toLowerCase();

        return name;
    }
}
