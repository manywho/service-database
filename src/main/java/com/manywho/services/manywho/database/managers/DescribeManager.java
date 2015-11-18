package com.manywho.services.manywho.database.managers;

import com.manywho.sdk.entities.draw.elements.type.TypeElement;
import com.manywho.sdk.entities.draw.elements.type.TypeElementBinding;
import com.manywho.sdk.entities.draw.elements.type.TypeElementBindingCollection;
import com.manywho.sdk.entities.draw.elements.type.TypeElementPropertyBinding;
import com.manywho.sdk.entities.security.AuthenticatedWho;
import com.manywho.services.manywho.database.services.DescribeService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DescribeManager {
    @Inject
    private DescribeService describeService;

    public List<TypeElementPropertyBinding> describeFields(AuthenticatedWho authenticatedWho) {
        List<TypeElementPropertyBinding> typeElementPropertyBindings = new ArrayList<>();

        typeElementPropertyBindings.add(new TypeElementPropertyBinding("ID", "ID", "string"));
        typeElementPropertyBindings.add(new TypeElementPropertyBinding("Name", "Name", "string"));
        typeElementPropertyBindings.add(new TypeElementPropertyBinding("Is Something?", "Is Something?", "boolean"));
        typeElementPropertyBindings.add(new TypeElementPropertyBinding("Date", "Date", "datetime"));

        return typeElementPropertyBindings;
    }

    public List<TypeElementBinding> describeTables(AuthenticatedWho authenticatedWho) throws Exception {
        if (authenticatedWho == null) {
            throw new Exception(("The AuthenticatedWho object cannot be null."));
        }

        if (authenticatedWho.getManyWhoTenantId() == null ||
                authenticatedWho.getManyWhoTenantId().isEmpty() == true) {
            throw new Exception("The AuthenticatedWho.ManyWhoTenantId cannot be null or blank.");
        }

        String databaseName = describeService.getDatabaseName(authenticatedWho);

        // Check if the tenant's database exists
        boolean tenantDatabaseExists = describeService.doesTenantDatabaseExist(databaseName);

        // Create the tenant's database if it doesn't exist
        if (!tenantDatabaseExists) {
            describeService.createTenantDatabase(databaseName);
        }

        // Get a list of all the tables in the database and build TypeElementBindings from them
        return describeService.listTenantTables(databaseName).stream()
                .map(s -> new TypeElementBinding(s, s, s))
                .collect(Collectors.toList());
    }

    public TypeElement describeBinding(AuthenticatedWho authenticatedWho, TypeElement typeElement) throws Exception {
        if (authenticatedWho == null) {
            throw new Exception(("The AuthenticatedWho object cannot be null."));
        }

        if (authenticatedWho.getManyWhoTenantId() == null ||
                authenticatedWho.getManyWhoTenantId().isEmpty() == true) {
            throw new Exception("The AuthenticatedWho.ManyWhoTenantId cannot be null or blank.");
        }

        if (typeElement == null) {
            throw new Exception("The TypeElement object cannot be null.");
        }

        // Create a safe binding from the type
        TypeElementBinding typeElementBinding = describeService.generateBinding(typeElement);

        // Assign the binding to the type - removing any existing bindings
        typeElement.setBindings(new TypeElementBindingCollection());
        typeElement.getBindings().add(typeElementBinding);

        // Remove the service element id from the Type as we don't want it to be managed by this service (as it will be
        // deleted on a service refresh and also not be editable in the draw tool
        typeElement.setServiceElementId(null);

        String databaseName = describeService.getDatabaseName(authenticatedWho);

        // Update the current schema if one already exists, otherwise create one
        boolean tenantTableExists = describeService.doesTenantTableExist(databaseName, typeElementBinding.getDatabaseTableName());
        if (tenantTableExists) {
            describeService.updateTenantTableSchema(databaseName, typeElementBinding.getDatabaseTableName(), typeElementBinding.getPropertyBindings());
        } else {
            describeService.createTenantTable(databaseName, typeElementBinding.getDatabaseTableName());
            describeService.createTenantTableSchema(databaseName, typeElementBinding.getDatabaseTableName(), typeElementBinding.getPropertyBindings());
        }

        return typeElement;
    }
}
