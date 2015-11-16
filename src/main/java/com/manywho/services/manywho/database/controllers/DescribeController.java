package com.manywho.services.manywho.database.controllers;

import com.manywho.sdk.entities.describe.DescribeServiceResponse;
import com.manywho.sdk.entities.draw.elements.type.TypeElement;
import com.manywho.sdk.entities.draw.elements.type.TypeElementBinding;
import com.manywho.sdk.entities.draw.elements.type.TypeElementPropertyBinding;
import com.manywho.sdk.entities.run.elements.config.ServiceRequest;
import com.manywho.sdk.entities.run.elements.type.ObjectDataRequest;
import com.manywho.sdk.entities.translate.Culture;
import com.manywho.sdk.services.annotations.AuthorizationRequired;
import com.manywho.sdk.services.controllers.AbstractController;
import com.manywho.sdk.services.describe.DescribeServiceBuilder;
import com.manywho.services.manywho.database.managers.DescribeManager;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.List;

@Path("/")
@Consumes("application/json")
@Produces("application/json")
public class DescribeController extends AbstractController {
    @Inject
    private DescribeManager describeManager;

    @Path("/metadata")
    @POST
    public DescribeServiceResponse describe(ServiceRequest serviceRequest) throws Exception {
        return new DescribeServiceBuilder()
                .setProvidesAutoBinding(true)
                .setProvidesDatabase(true)
                .setCulture(new Culture("EN", "US"))
                .createDescribeService()
                .createResponse();
    }

    @Path("/metadata/binding")
    @POST
    @AuthorizationRequired
    public TypeElement describeBinding(TypeElement typeElement) throws Exception {
        return describeManager.describeBinding(getAuthenticatedWho(), typeElement);
    }

    @Path("/metadata/table")
    @POST
    @AuthorizationRequired
    public List<TypeElementBinding> describeTables(ObjectDataRequest objectDataRequest) throws Exception {
        return describeManager.describeTables(getAuthenticatedWho());
    }

    @Path("/metadata/field")
    @POST
    @AuthorizationRequired
    public List<TypeElementPropertyBinding> describeFields(ObjectDataRequest objectDataRequest) {
        return describeManager.describeFields(getAuthenticatedWho());
    }
}