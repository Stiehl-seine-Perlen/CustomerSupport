package de.benevolo.customer.support.services.impl;

import de.benevolo.customer.support.services.AttachmentFileService;
import io.quarkus.test.Mock;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

import javax.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This is a mock implementation of the {@link AttachmentFileService} used in unit tests in order to avoid
 * connecting to a real s3
 *
 * @author Daniel Mehlber
 */
@ApplicationScoped
@Mock
public class TestAttachmentFileService implements AttachmentFileService {

    private final Map<String, byte[]> files = new HashMap<>();

    @Override
    public String uploadAttachmentContent(final InputPart fileInputPart, final int fileInputSize) throws Exception {
        final String location = UUID.randomUUID().toString();
        final InputStream inputStream = fileInputPart.getBody(InputStream.class, null);
        final byte[] content = inputStream.readAllBytes();

        files.put(location, content);
        return location;
    }

    @Override
    public InputStream downloadAttachmentContent(final String location) {
        final byte[] file = files.get(location);
        return new ByteArrayInputStream(file);
    }
}
