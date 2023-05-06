package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.*;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.jboss.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@QuarkusTest
public class PrepareIssueServiceTest {

    @Inject
    Logger log;

    @Inject
    PrepareIssueService service;

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    @BeforeEach
    @Transactional
    public void prepare() {
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("should create issue from valid support request")
    public void requestToIssue() {
        // prepare test data
        final SupportIssue someIssue = TestSupportIssues.getRandomValid();
        final SupportIssueMessage someMessage = TestSupportIssueMessages.getRandomValid();

        final int attachmentCount = 5;
        final Set<Attachment> attachments = new HashSet<>();
        for (int i = 0; i < attachmentCount; i++) {
            final Attachment attachment = TestAttachments.getRandomValid();
            attachments.add(attachment);
        }
        persistAttachments(attachments);

        final Set<UUID> attachmentIds = attachments.stream().map(Attachment::getId).collect(Collectors.toSet());

        final SupportRequest validRequest = new SupportRequest(
                someIssue.getTitle(),
                someMessage.getMessage(),
                someIssue.getIssuerEmailAddress(),
                attachmentIds);

        // use service to create issue
        final Long issueId = service.createSupportIssue(validRequest);

        // assert correctness
        final SupportIssue persistedIssue = loadIssue(issueId);
        Assertions.assertNotNull(persistedIssue);
        Assertions.assertEquals(someIssue.getTitle(), persistedIssue.getTitle(), "incorrect title persisted");
        Assertions.assertEquals(someIssue.getIssuerEmailAddress(), persistedIssue.getIssuerEmailAddress(), "incorrect email persisted");
        Assertions.assertEquals(SupportIssueStatus.OPEN, persistedIssue.getStatus());

        final List<SupportIssueMessage> persistedMessages = persistedIssue.getMessages();
        Assertions.assertNotNull(persistedMessages, "set of messages was null");
        Assertions.assertEquals(1, persistedMessages.size(), "there must be exactly 1 message after creation");

        final SupportIssueMessage persistedFirstMessage = persistedMessages.stream().findFirst().orElse(null);
        Assertions.assertNotNull(persistedFirstMessage, "first message must not be null");
        Assertions.assertEquals(someMessage.getMessage(), persistedFirstMessage.getMessage(), "wrong message content persisted");
        Assertions.assertTrue(persistedFirstMessage.isFromCustomer(), "first message must be from customer");

        final Set<Attachment> persistedAttachments = persistedFirstMessage.getAttachments();
        Assertions.assertNotNull(persistedAttachments, "attachments must not be null");
        Assertions.assertEquals(attachments.size(), persistedAttachments.size(), "not enough attachments were persisted");
    }

    @Test
    @DisplayName("should not create issue from invalid support request")
    public void invalidRequestToIssue() {
        // prepare test data
        final SupportIssue someIssue = TestSupportIssues.getRandomInvalid();
        final SupportIssueMessage someMessage = TestSupportIssueMessages.getRandomInvalid();

        final int attachmentCount = 5;
        final Set<Attachment> attachments = new HashSet<>();
        for (int i = 0; i < attachmentCount; i++) {
            final Attachment attachment = TestAttachments.getRandomValid();
            attachments.add(attachment);
        }
        persistAttachments(attachments);

        final Set<UUID> attachmentIds = attachments.stream().map(Attachment::getId).collect(Collectors.toSet());

        final SupportRequest validRequest = new SupportRequest(
                someIssue.getTitle(),
                someMessage.getMessage(),
                someIssue.getIssuerEmailAddress(),
                attachmentIds);

        // use service to create issue
        Assertions.assertThrows(Exception.class, () -> service.createSupportIssue(validRequest));
    }

    @Transactional
    void persistAttachments(final Set<Attachment> attachments) {
        log.debugf("persisting %d attachments", attachments.size());
        for (final Attachment attachment : attachments) {
            attachmentRepository.persist(attachment);
        }
    }

    @Transactional
    SupportIssue loadIssue(final Long issueId) {
        final SupportIssue issue = issueRepository.findById(issueId);

        // force loading all children (due to lazy loading) and assert that issue is not null
        Assertions.assertDoesNotThrow(() -> issue.getMessages()
                .forEach(message -> message.getAttachments()
                        .forEach(Attachment::getId)));

        return issue;
    }

}
