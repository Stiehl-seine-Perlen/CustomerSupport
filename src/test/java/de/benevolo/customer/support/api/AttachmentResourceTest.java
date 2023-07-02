package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.Attachment;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueMessage;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class AttachmentResourceTest {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    SupportIssueRepository issueRepository;

    @BeforeEach
    @Transactional
    public void prepare() {
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();
        issueRepository.deleteAll();
    }

    private void uploadTestAttachment(final Attachment attachment, final int expectedStatusCode) throws IOException {

        final InputStream fileStream = getClass().getResourceAsStream("/test.txt");
        final File file = new File(getClass().getResource("/test.txt").getFile());
        final long fileSize = file.length();

        given().contentType(ContentType.MULTIPART)
                .multiPart("uuid", attachment.getId().toString())
                .multiPart("file", attachment.getFilename(), fileStream)
                .multiPart("fileSize", fileSize)
                .when().post("/support/attachment")
                .then().statusCode(expectedStatusCode);

        if (fileStream != null)
            fileStream.close();

    }

    @Test
    @DisplayName("valid attachments should be uploaded")
    @Transactional
    public void uploadValidAttachment() throws IOException {
        // -- ARRANGE --
        final Attachment initialAttachment = TestAttachments.getRandomValid();

        // -- ACT --
        uploadTestAttachment(initialAttachment, 200);

        // -- ASSERT --
        final Attachment fetchedAttachment = attachmentRepository.findById(initialAttachment.getId());

        // attachment location must have been updated
        assertNotEquals(fetchedAttachment.getLocation(), initialAttachment.getLocation());
        initialAttachment.setLocation(fetchedAttachment.getLocation());

        assertNotNull(fetchedAttachment);
        assertEquals(initialAttachment, fetchedAttachment);

        // assert that file has been uploaded and can be downloaded
        final byte[] fetchedFile = given().pathParam("id", initialAttachment.getId().toString())
                .when().get("/support/attachment/{id}/download")
                .then().statusCode(200).and().extract().asByteArray();

        final String fetchedBytesAsString = new String(fetchedFile);
        final String expectedBytesAsString = new String(Files.readAllBytes(Paths.get(getClass().getResource("/test.txt").getPath())));

        assertEquals(expectedBytesAsString, fetchedBytesAsString);
    }

    @Test
    @DisplayName("invalid attachments should not be uploaded")
    @Transactional
    public void uploadInvalidAttachment() throws IOException {
        // -- ARRANGE --
        final Attachment attachment = TestAttachments.getRandomInvalid();

        // -- ACT --
        uploadTestAttachment(attachment, 400);

        // -- ASSERT --
        final Attachment fetchedAttachment = attachmentRepository.findById(attachment.getId());

        assertNull(fetchedAttachment);
    }

    @Test
    @DisplayName("double UUID should be rejected")
    @Transactional
    public void uploadDoubleAttachments() throws IOException {
        // -- ARRANGE --
        final Attachment attachment1 = TestAttachments.getRandomValid();
        final Attachment attachment2 = TestAttachments.getRandomValid();
        attachment1.setId(attachment2.getId());

        // -- ACT --
        uploadTestAttachment(attachment1, 200);
        uploadTestAttachment(attachment2, 409);

        // -- ASSERT --
        final Attachment persistedAttachment = attachmentRepository.findById(attachment1.getId());
        assertNotNull(persistedAttachment);
    }

    @Test
    @DisplayName("removing loose attachment should work")
    public void removeLooseAttachment() {
        // -- ARRANGE --
        final Attachment attachment = TestAttachments.getRandomValid();
        persistAttachment(attachment);

        // -- ACT --
        given().pathParam("id", attachment.getId())
                .when().delete("/support/attachment/{id}")
                .then().statusCode(200);

        // -- ASSERT --
        assertNull(fetchAttachment(attachment.getId()));
    }

    @Transactional
    public void persistAttachment(final Attachment attachment) {
        attachmentRepository.persist(attachment);
    }

    @Transactional
    public Attachment fetchAttachment(final UUID id) {
        return attachmentRepository.findById(id);
    }


    @Transactional
    public void persistEntitiesConnected(final SupportIssue issue, final SupportIssueMessage message, final Attachment attachment) {
        issue.addMessage(message);
        message.addAttachment(attachment);

        issueRepository.persist(issue);
        messageRepository.persist(message);
        attachmentRepository.persist(attachment);
    }

    @Test
    @DisplayName("removing a used attachment should not work")
    public void removeUsedAttachment() {
        // -- ARRANGE --
        final SupportIssue issue = TestSupportIssues.getRandomValid();
        final SupportIssueMessage message = TestSupportIssueMessages.getRandomValid();
        final Attachment attachment = TestAttachments.getRandomValid();

        persistEntitiesConnected(issue, message, attachment);

        // -- ACT --
        given().pathParam("id", attachment.getId())
                .when().delete("/support/attachment/{id}")
                .then().statusCode(409);

        // -- ASSERT --
        final Attachment stillExistingAttachment = fetchAttachment(attachment.getId());
        assertNotNull(stillExistingAttachment);
    }

    @Test
    @DisplayName("getting existing attachment should work")
    public void getExistingAttachment() {
        // -- ARRANGE --
        final Attachment attachment = TestAttachments.getRandomValid();

        persistAttachment(attachment);

        // -- ACT --
        final Attachment returnedAttachment = given().pathParam("id", attachment.getId())
                .when().get("/support/attachment/{id}")
                .then().statusCode(200).and().extract().body().as(Attachment.class);

        // -- ASSERT --
        assertNotNull(returnedAttachment);
        assertEquals(attachment, returnedAttachment);
    }

    @Test
    @DisplayName("getting unknown attachment should not work")
    public void getUnknownAttachment() {
        // -- ARRANGE --
        final UUID id = UUID.randomUUID();

        // -- ACT & ASSERT --
        given().pathParam("id", id)
                .when().get("/support/attachment/{id}")
                .then().statusCode(410);
    }
}
