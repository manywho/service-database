package com.manywho.services.manywho.database.services;

import com.google.common.collect.ImmutableMap;
import com.manywho.sdk.entities.run.elements.type.ListFilter;
import com.manywho.sdk.entities.run.elements.type.ObjectCollection;
import com.manywho.sdk.entities.run.elements.type.Property;
import com.manywho.sdk.entities.run.elements.type.PropertyCollection;
import com.manywho.sdk.enums.ContentType;
import com.manywho.services.manywho.database.entities.Schema;
import com.rethinkdb.RethinkDB;
import com.rethinkdb.gen.ast.Table;
import com.rethinkdb.net.Connection;
import com.rethinkdb.net.Cursor;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class DatabaseLoadService {
    @Inject
    private RethinkDB rethinkDB;

    @Inject
    private Connection<?> connection;

    public ObjectCollection buildResultObjects(Cursor<Map<String, Object>> cursor, Schema schema, String tableName) {
        Stream<Map<String, java.lang.Object>> stream = StreamSupport.stream(cursor.spliterator(), false);

        return stream.map(row -> {
            com.manywho.sdk.entities.run.elements.type.Object object = new com.manywho.sdk.entities.run.elements.type.Object();

            PropertyCollection properties = new PropertyCollection();
            for (Map.Entry<String, ContentType> field : schema.getFields().entrySet()) {
                properties.add(new Property(field.getKey(), row.get(field.getKey()), field.getValue()));
            }

            object.setDeveloperName(tableName);
            object.setExternalId(String.valueOf(row.get("id")));
            object.setProperties(properties);

            return object;
        }).collect(Collectors.toCollection(ObjectCollection::new));
    }

    public Cursor<Map<String, java.lang.Object>> loadResults(Table table, ListFilter listFilter) {
        // TODO: Somehow we have to make this filtering dynamic as we can't declaratively construct the filter
        return table.limit(listFilter.getLimit())
                .map(object -> {
                    // TODO: Make this check against the stored predefined schema to see if the field is a date
                    return rethinkDB.branch(
                            object.hasFields("Date"),
                            object.merge(ImmutableMap.of("Date", object.getField("Date").toIso8601())),
                            object
                    );
                })
                .run(connection);
    }
}
