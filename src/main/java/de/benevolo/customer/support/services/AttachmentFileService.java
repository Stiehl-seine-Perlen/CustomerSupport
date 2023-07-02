package de.benevolo.customer.support.services;

import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.InputStream;

public interface AttachmentFileService {

    String uploadAttachmentContent(final InputPart fileInputPart, final int fileInputSize) throws Exception;

    InputStream downloadAttachmentContent(final String location) throws Exception;

    default String extractFilename(final InputPart inputPart) {
        final MultivaluedMap<String, String> headers = inputPart.getHeaders();
        final String contentDisposition = headers.getFirst(HttpHeaders.CONTENT_DISPOSITION);
        final String[] parts = contentDisposition.split(";");

        for (final String part : parts) {
            if (part.trim().startsWith("filename")) {
                final String[] fileNamePart = part.split("=");
                return fileNamePart[1].trim().replaceAll("\"", "");

            }
        }

        return "";
    }

}
