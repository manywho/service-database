package com.manywho.services.manywho.database.managers;

import com.github.fge.lambdas.Throwing;
import com.manywho.sdk.entities.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.entities.run.elements.type.ObjectDataResponse;
import com.manywho.sdk.entities.security.AuthenticatedWho;
import com.manywho.services.manywho.database.entities.Schema;
import com.manywho.services.manywho.database.services.DatabaseLoadService;
import com.manywho.services.manywho.database.services.DatabaseSaveService;
import com.manywho.services.manywho.database.services.DatabaseService;
import com.rethinkdb.gen.ast.Db;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Cursor;

import javax.inject.Inject;
import java.util.Map;

public class DatabaseManager {
    @Inject
    private DatabaseService databaseService;

    @Inject
    private DatabaseLoadService databaseLoadService;

    @Inject
    private DatabaseSaveService databaseSaveService;

    public ObjectDataResponse loadData(AuthenticatedWho authenticatedWho, ObjectDataRequest objectDataRequest) throws Exception {
        String databaseName = authenticatedWho.getManyWhoTenantId().replace("-", "_");

        String tableName = objectDataRequest.getObjectDataType().getDeveloperName();

        // Fetch the needed database and table objects
        Db database = databaseService.loadTenantDatabase(databaseName);
        Table table = databaseService.loadTenantTable(database, tableName);

        Schema schema = databaseService.loadTenantSchemaAsPojo(database, tableName);

        // Load the results from the database, based on the given ListFilter
        Cursor<Map<String, Object>> cursor = databaseLoadService.loadResults(table, objectDataRequest.getListFilter());

        // Build the ManyWho objects, and return them in a response
        return new ObjectDataResponse(databaseLoadService.buildResultObjects(cursor, schema, tableName));
    }

    public ObjectDataResponse saveData(AuthenticatedWho authenticatedWho, ObjectDataRequest objectDataRequest) throws Exception {
        String databaseName = authenticatedWho.getManyWhoTenantId().replace("-", "_");

        // Fetch the needed database object
        Db database = databaseService.loadTenantDatabase(databaseName);

        // Loop over all the given objects and save them
        // TODO: Check with Steve if there is a better way to block updates on a field
        objectDataRequest.getObjectData().stream()
                .filter(object -> !object.getDeveloperName().equalsIgnoreCase("ID"))
                .forEach(Throwing.consumer(object -> {
                    Table table = databaseService.loadTenantTable(database, object.getDeveloperName());

                    databaseSaveService.saveObject(table, object.getProperties());
                }));

        return new ObjectDataResponse();
    }
}
