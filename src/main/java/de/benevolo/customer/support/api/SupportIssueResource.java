package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/support/issue/{id}")
public class SupportIssueResource {

    @Inject
    SupportIssueRepository supportIssueRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SupportIssue getIssue(@PathParam("id") final Long id) {
        return supportIssueRepository.findById(id);
    }

}
