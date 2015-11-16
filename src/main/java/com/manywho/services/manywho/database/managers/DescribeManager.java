package com.manywho.services.manywho.database.managers;

import com.manywho.sdk.entities.draw.elements.type.*;
import com.manywho.sdk.entities.security.AuthenticatedWho;
import com.manywho.services.manywho.database.services.DescribeService;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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

    public List<TypeElementBinding> describeTables(AuthenticatedWho authenticatedWho) {
        String databaseName = authenticatedWho.getManyWhoTenantId().replace("-", "_");

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
        String databaseName = authenticatedWho.getManyWhoTenantId().replace("-", "_");

        // Update the current schema if one already exists, otherwise create one
        boolean tenantTableExists = describeService.doesTenantTableExist(databaseName, typeElement.getDeveloperName());
        if (tenantTableExists) {
            describeService.updateTenantTableSchema(databaseName, typeElement.getDeveloperName(), typeElement.getProperties());
        } else {
            describeService.createTenantTable(databaseName, typeElement.getDeveloperName());
            describeService.createTenantTableSchema(databaseName, typeElement.getDeveloperName(), typeElement.getProperties());
        }

        TypeElementPropertyBindingCollection typeElementPropertyBindings = new TypeElementPropertyBindingCollection();

        for (TypeElementProperty property : typeElement.getProperties()) {
            TypeElementPropertyBinding typeElementPropertyBinding = new TypeElementPropertyBinding(property.getDeveloperName(), property.getDeveloperName());

            // TODO: THIS SHOULD DEFINITELY NOT BE IN THE SERVICE, YO
            if (StringUtils.isEmpty(property.getId())) {
               property.setId(UUID.randomUUID().toString());
            }

            typeElementPropertyBinding.setTypeElementPropertyId(property.getId());

            typeElementPropertyBindings.add(typeElementPropertyBinding);
        }

        TypeElementBinding typeElementBinding = new TypeElementBinding(typeElement.getDeveloperName(), typeElement.getDeveloperName(), typeElement.getDeveloperName(), typeElementPropertyBindings);
        typeElementBinding.setServiceElementId(typeElement.getServiceElementId());

        TypeElementBindingCollection bindings = new TypeElementBindingCollection();
        bindings.add(typeElementBinding);

        typeElement.setBindings(bindings);

        return typeElement;
    }
}
