package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/issue/{id}")
public class SupportIssueResource {

    private static final Logger log = LoggerFactory.getLogger(SupportIssueResource.class);

    @Inject
    SupportIssueRepository supportIssueRepository;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SupportIssue getIssue(@PathParam("id") final Long id) {
        log.info("requested issue {}", id);
        return supportIssueRepository.findById(id);
    }

}
