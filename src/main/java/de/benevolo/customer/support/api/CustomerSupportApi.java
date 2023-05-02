package de.benevolo.customer.support.api;


import de.benevolo.customer.support.entities.SupportRequest;
import org.jboss.logging.Logger;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/support/")
public class CustomerSupportApi {

    @Inject
    ProcessService processes;

    @Inject
    @Named("CustomerSupport")
    Process<? extends Model> customerSupportProcess;

    @Inject
    Logger log;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/request")
    public Response requestSupport(final @Valid SupportRequest request) {
        log.debug("received support request");

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("request", request);

        final Model model = customerSupportProcess.createModel();
        model.fromMap(parameters);

        processes.createProcessInstance((Process<Model>) customerSupportProcess, "", model, null);

        return Response.ok().build();
    }

}
