package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

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
        final List<Attachment> attachments = TestAttachments.getAllValidCombinations();

        for (final Attachment attachment : attachments) {
            final Set<ConstraintViolation<Attachment>> violations = validator.validate(attachment);
            Assertions.assertEquals(0, violations.size(), "valid attachment has violations");
        }
    }

    @Test
    @DisplayName("invalid attachments should not pass")
    public void validateInvalid() {
        final List<Attachment> attachments = TestAttachments.getAllInvalidCombinations();

        for (final Attachment attachment : attachments) {
            final Set<ConstraintViolation<Attachment>> violations = validator.validate(attachment);
            Assertions.assertNotEquals(0, violations.size(), "invalid attachment has no violations");
        }
    }

    @Test
    @DisplayName("test creation of attachments with message")
    @Transactional
    public void createWithMessage() {
        final Attachment originalAttachment = TestAttachments.getRandomValid();

        // add attachment to message (required)
        final SupportIssueMessage currentMessage = entityManager.find(SupportIssueMessage.class, currentMessageId);
        currentMessage.addAttachment(originalAttachment);

        entityManager.persist(originalAttachment);

        entityManager.flush();

        final Attachment persistedAttachment = attachmentRepository.findById(originalAttachment.getId());
        Assertions.assertEquals(originalAttachment, persistedAttachment);
    }

    @Test
    @DisplayName("test creation of attachments without message")
    @Transactional
    public void createWithoutMessage() {
        final Attachment originalAttachment = TestAttachments.getRandomValid();

        entityManager.persist(originalAttachment);

        entityManager.flush();

        final Attachment persistedAttachment = attachmentRepository.findById(originalAttachment.getId());
        Assertions.assertEquals(originalAttachment, persistedAttachment);
    }

}