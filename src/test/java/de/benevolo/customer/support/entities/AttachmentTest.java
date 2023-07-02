package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@QuarkusTest
public class AttachmentTest {

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    private Long currentMessageId;

    @BeforeEach
    @Transactional
    public void prepare() {
        attachmentRepository.deleteAll();

        final SupportIssueMessage message = TestSupportIssueMessages.getRandomValid();
        final SupportIssue issue = TestSupportIssues.getRandomValid();
        issue.addMessage(message);

        issueRepository.persist(issue);
        messageRepository.persist(message);

        currentMessageId = message.getId();
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        attachmentRepository.deleteAll();
        issueRepository.deleteAll();
        messageRepository.deleteAll();
    }

    @Test
    @DisplayName("valid attachments should pass")
    public void validateValid() {
        // -- ARRANGE --
        final List<Attachment> attachments = TestAttachments.getAllValidCombinations();

        // -- ACT & ASSERT --
        for (final Attachment attachment : attachments) {
            final Set<ConstraintViolation<Attachment>> violations = validator.validate(attachment);
            assertEquals(0, violations.size(), "valid attachment has violations");
        }
    }

    @Test
    @DisplayName("invalid attachments should not pass")
    public void validateInvalid() {
        // -- ARRANGE --
        final List<Attachment> attachments = TestAttachments.getAllInvalidCombinations();

        // -- ACT & ASSERT --
        for (final Attachment attachment : attachments) {
            final Set<ConstraintViolation<Attachment>> violations = validator.validate(attachment);
            assertNotEquals(0, violations.size(), "invalid attachment has no violations");
        }
    }

    @Test
    @DisplayName("test creation of attachments with message")
    @Transactional
    public void createWithMessage() {
        // -- ARRANGE --
        final Attachment originalAttachment = TestAttachments.getRandomValid();

        // add attachment to message (required)
        final SupportIssueMessage currentMessage = entityManager.find(SupportIssueMessage.class, currentMessageId);
        currentMessage.addAttachment(originalAttachment);

        // -- ACT --
        entityManager.persist(originalAttachment);
        entityManager.flush();

        // -- ASSERT --
        final Attachment persistedAttachment = attachmentRepository.findById(originalAttachment.getId());
        assertEquals(originalAttachment, persistedAttachment);
    }

    @Test
    @DisplayName("test creation of attachments without message")
    @Transactional
    public void createWithoutMessage() {
        // -- ARRANGE --
        final Attachment originalAttachment = TestAttachments.getRandomValid();

        // -- ACT --
        entityManager.persist(originalAttachment);
        entityManager.flush();

        // -- ASSERT --
        final Attachment persistedAttachment = attachmentRepository.findById(originalAttachment.getId());
        assertEquals(originalAttachment, persistedAttachment);
    }

}