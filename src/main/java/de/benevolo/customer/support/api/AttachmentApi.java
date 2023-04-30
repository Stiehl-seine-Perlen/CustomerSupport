package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.entities.Attachment;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("support/attachment/")
public class AttachmentApi {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    Logger log;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "uploads attachment to platform",
            description = "The attachment will be uploaded and stay unused (loose) until a message was uploaded")
    @APIResponses({
            @APIResponse(responseCode = "409 Conflict", description = "UUID already exists and attachment can not be persisted"),
            @APIResponse(responseCode = "200 Ok", description = "attachment was uploaded and can be referenced")})
    @Transactional
    public Response uploadAttachment(final @Valid Attachment attachment) {
        log.debugf("uploading attachment with uuid %s was requested...", attachment.getId());

        final boolean isUuidAlreadyTaken = attachmentRepository.findByIdOptional(attachment.getId()).isPresent();
        if (isUuidAlreadyTaken) {
            log.warn("uuid collision: the uuid already exists and attachment cannot be persisted");
            return Response.status(Response.Status.CONFLICT).build();
        }

        attachmentRepository.persist(attachment);

        log.infof("upload of new attachment %s was successful", attachment.getId());
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "deleted loose attachment",
            description = "The attachment is deleted, if it is not attached to any message and therefore unused")
    @APIResponses({
            @APIResponse(responseCode = "409 Conflict", description = "cannot delete attachment because it is in use (attached to message)"),
            @APIResponse(responseCode = "200 Ok", description = "attachment was deleted"),
            @APIResponse(responseCode = "410 Gone", description = "attachment UUID unknown")})
    @Transactional
    public Response deleteAttachment(@PathParam("id") @Valid final UUID id) {
        log.debugf("deleting attachment %s was requested...", id);

        final Attachment attachment = attachmentRepository.findById(id);

        final boolean attachmentExists = attachment != null;
        if (!attachmentExists) {
            log.warnf("cannot delete attachment %s because it does not exist", id);
            return Response.status(Response.Status.GONE).build();
        }

        final boolean attachmentCanBeDeleted = attachment.getMessage() == null;
        if (attachmentCanBeDeleted) {
            attachmentRepository.delete(attachment);
        } else {
            log.warnf("cannot delete attachment %s because it is still bound to a message", id);
            return Response.status(Response.Status.CONFLICT).build();
        }

        log.infof("deleted attachment %s successfully", id);
        return Response.ok().build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "get attachment",
            description = "Returns the attachment with the selected id")
    @APIResponses({
            @APIResponse(responseCode = "200 Ok", description = "attachment found and returned", content = @Content(schema = @Schema(implementation = Attachment.class))),
            @APIResponse(responseCode = "410 Gone", description = "attachment UUID unknown"),})
    @Transactional
    public Response viewAttachment(@PathParam("id") final UUID id) {
        final Attachment attachment = attachmentRepository.findById(id);

        if (attachment != null) {
            return Response.ok().entity(attachment).build();
        } else {
            return Response.status(Response.Status.GONE).build();
        }
    }

}
