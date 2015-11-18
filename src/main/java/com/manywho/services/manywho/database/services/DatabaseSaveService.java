package com.manywho.services.manywho.database.services;

import com.manywho.sdk.entities.run.elements.type.Property;
import com.manywho.sdk.enums.ContentType;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import org.joda.time.DateTime;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseSaveService {

    @Inject
    private Connection<?> connection;

    public void saveObject(Table table, List<Property> properties) throws Exception {
        Map<String, Object> dataToInsert = new HashMap<>();

        for (Property property : properties) {
            dataToInsert.put(property.getDeveloperName(), parseContentValue(property.getContentValue(), property.getContentType()));
        }

        table.insert(dataToInsert).run(connection);
    }

    private static java.lang.Object parseContentValue(String contentValue, ContentType contentType) throws Exception {
        switch (contentType) {
            case Boolean:
                return Boolean.parseBoolean(contentValue);
            case DateTime:
                return DateTime.parse(contentValue).toDate();
            case Number:
                return Float.parseFloat(contentValue);
            case List:
            case Object:
                // We do not store objects and lists in the object, we store those separately
                return null;
            case Content:
            case Password:
            case String:
            default:
                return contentValue;
        }
    }
}
