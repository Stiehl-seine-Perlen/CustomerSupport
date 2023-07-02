package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.entities.Attachment;
import de.benevolo.customer.support.services.AttachmentFileService;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Path("support/attachment/")
public class AttachmentResource {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    AttachmentFileService attachmentFileService;

    @Inject
    Validator validator;

    private static final Logger LOG = LoggerFactory.getLogger(AttachmentResource.class);

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "uploads attachment to platform",
            description = "The attachment will be uploaded and stay unused (loose) until a message was uploaded")
    @APIResponses({
            @APIResponse(responseCode = "409 Conflict", description = "UUID already exists and attachment can not be persisted"),
            @APIResponse(responseCode = "200 Ok", description = "attachment was uploaded and can be referenced")})
    @Transactional
    public Response uploadAttachment(final MultipartFormDataInput input) {

        final Attachment attachment;
        final InputPart attachmentFile;
        final int fileInputSize;

        // extract multipart form data
        final Map<String, List<InputPart>> formParts = input.getFormDataMap();
        try {
            // upload file to s3
            attachmentFile = formParts.get("file").get(0);
            fileInputSize = Integer.parseInt(formParts.get("fileSize").get(0).getBodyAsString());
            final String uuid = formParts.get("uuid").get(0).getBodyAsString();

            // Extract the filename from the content disposition
            final String filename = attachmentFileService.extractFilename(attachmentFile);

            attachment = new Attachment(UUID.fromString(uuid), filename, null);
        } catch (final IOException error) {
            LOG.error("cannot upload attachment: {}", error.getMessage(), error);
            return Response.serverError().build();
        }

        // check extracted values
        final Set<ConstraintViolation<Attachment>> violations = validator.validate(attachment);
        if (violations.size() > 0) {
            final String violationStr = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining("; "));
            LOG.warn("cannot upload invalid attachment: {}", violationStr);
            return Response.status(Response.Status.BAD_REQUEST).entity(violationStr).build();
        }

        // check if UUID is already in use
        final boolean isUuidAlreadyTaken = attachmentRepository.findByIdOptional(attachment.getId()).isPresent();
        if (isUuidAlreadyTaken) {
            LOG.warn("uuid collision: the uuid already exists and attachment cannot be persisted");
            return Response.status(Response.Status.CONFLICT).build();
        }

        // try to upload the attachment
        try {
            final String attachmentLocation = attachmentFileService.uploadAttachmentContent(attachmentFile, fileInputSize);
            attachment.setLocation(attachmentLocation);
        } catch (final Exception uploadFailed) {
            LOG.error("upload of attachment to s3 failed: {}", uploadFailed.getMessage(), uploadFailed);
            return Response.serverError().build();
        }

        attachmentRepository.persist(attachment);

        LOG.info("upload of new attachment {} (at location '{}') was successful", attachment.getId(), attachment.getLocation());
        return Response.ok().build();
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "deleted loose attachment",
            description = "The attachment is deleted, if it is not attached to any message and therefore unused")
    @APIResponses({
            @APIResponse(responseCode = "409 Conflict", description = "cannot delete attachment because it is in use (attached to message)"),
            @APIResponse(responseCode = "200 Ok", description = "attachment was deleted"),
            @APIResponse(responseCode = "410 Gone", description = "attachment UUID unknown")})
    @Transactional
    public Response deleteAttachment(@PathParam("id") @Valid final UUID id) {
        LOG.debug("deleting attachment {} was requested...", id);

        final Attachment attachment = attachmentRepository.findById(id);

        final boolean attachmentExists = attachment != null;
        if (!attachmentExists) {
            LOG.warn("cannot delete attachment {} because it does not exist", id);
            return Response.status(Response.Status.GONE).build();
        }

        final boolean attachmentCanBeDeleted = attachment.getMessage() == null;
        if (attachmentCanBeDeleted) {
            attachmentRepository.delete(attachment);
        } else {
            LOG.warn("cannot delete attachment {} because it is still bound to a message", id);
            return Response.status(Response.Status.CONFLICT).build();
        }

        LOG.info("deleted attachment {} successfully", id);
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

    @GET
    @Path("{id}/download")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "get content of attachment file",
            description = "Returns the attachment file content with the selected id")
    @APIResponses({
            @APIResponse(responseCode = "200 Ok", description = "attachment found and returned", content = @Content(schema = @Schema(implementation = Attachment.class))),
            @APIResponse(responseCode = "410 Gone", description = "attachment UUID unknown"),})
    @Transactional
    public Response viewAttachmentFileContent(@PathParam("id") final UUID id) {
        LOG.info("file contents of attachment {} has been requested", id);
        final Attachment attachment = attachmentRepository.findById(id);

        if (attachment == null) {
            LOG.warn("attachment with id {} does not exist (amymore)", id);
            return Response.status(Response.Status.GONE).build();
        }

        try {
            // auto-closable can be ignored because stream is already closed on error
            final InputStream stream = attachmentFileService.downloadAttachmentContent(attachment.getLocation());
            final Response response = Response.ok()
                    .entity(stream)
                    .header("Content-Disposition", "attachment; filename=\"" + attachment.getFilename() + "\"")
                    .build();

            // close stream before returning
            if (stream != null) {
                stream.close();
            }

            return response;
        } catch (final Exception e) {
            LOG.error("failed to download attachment file content: {}", e.getMessage(), e);
            return Response.serverError().build();
        }
    }

}
