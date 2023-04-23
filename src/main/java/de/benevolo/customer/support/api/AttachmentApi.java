package de.benevolo.customer.support.api;

import com.oracle.svm.core.annotate.Delete;
import de.benevolo.customer.support.entities.Attachment;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("support/attachment/{id}")
public class AttachmentApi {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response uploadAttachment(final Attachment attachment) {
        return Response.status(Response.Status.GONE).build();
    }

    @Delete
    public Response deleteAttachment(@PathParam("id") final UUID id) {
        return Response.status(Response.Status.GONE).build();
    }

    @GET
    public Response viewAttachment(@PathParam("id") final UUID id) {
        return Response.status(Response.Status.GONE).build();
    }

}
