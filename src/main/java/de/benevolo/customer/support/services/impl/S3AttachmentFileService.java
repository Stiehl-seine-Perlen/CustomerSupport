package de.benevolo.customer.support.services.impl;

import de.benevolo.customer.support.services.AttachmentFileService;
import de.benevolo.minio.S3Service;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;

@ApplicationScoped
public class S3AttachmentFileService implements AttachmentFileService {

    @Inject
    S3Service s3;

    /**
     * Uploads the attachment to the s3 bucket.
     *
     * @param fileInputPart the file input part which contains the input stream of the file bytes
     * @param fileInputSize the file size required be s3
     * @return the location identifier on the s3
     * @throws Exception something went wrong during upload (unspecified)
     */
    public String uploadAttachmentContent(final InputPart fileInputPart, final int fileInputSize) throws Exception {
        final MultivaluedMap<String, String> headers = fileInputPart.getHeaders();
        final String encType = headers.getFirst("Content-Type").split("/")[1];
        final InputStream inputStream = fileInputPart.getBody(InputStream.class, null);
        return s3.putObject(inputStream, encType, fileInputSize);
    }

    /**
     * Downloads the attachment file content from the s3 bucket
     *
     * @param location location identifier of the file on the s3 bucket
     * @return input stream of the attachment file bytes
     * @throws Exception something went wrong during download (unspecified)
     */
    public InputStream downloadAttachmentContent(final String location) throws Exception {
        return s3.getObject(location);
    }

}
