package de.benevolo.customer.support.entities;

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

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class SupportIssueMessageTest {

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    Validator validator;

    @Inject
    EntityManager entityManager;

    private Long currentSupportIssueId;

    @BeforeEach
    @Transactional
    public void prepare() {
        messageRepository.deleteAll();
        issueRepository.deleteAll();

        final SupportIssue currentSupportIssue = TestSupportIssues.getRandomValid();
        issueRepository.persist(currentSupportIssue);
        currentSupportIssueId = currentSupportIssue.getId();
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        messageRepository.deleteAll();
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("valid messages should pass")
    public void validateValid() {
        final List<SupportIssueMessage> validMessages = TestSupportIssueMessages.getAllValidCombinations();

        for (final SupportIssueMessage message : validMessages) {
            final Set<ConstraintViolation<SupportIssueMessage>> violations = validator.validate(message);
            assertEquals(0, violations.size(), "there are violations in valid messages");
        }
    }

    @Test
    @DisplayName("invalid messages should not pass")
    public void validateInvalid() {
        final List<SupportIssueMessage> invalidMessages = TestSupportIssueMessages.getAllInvalidCombinations();

        for (final SupportIssueMessage message : invalidMessages) {
            final Set<ConstraintViolation<SupportIssueMessage>> violations = validator.validate(message);
            assertNotEquals(0, violations.size(), "there are no violations in invalid messages");
        }
    }

    @Test
    @DisplayName("creating a message with issue should work")
    @Transactional
    public void createWithIssue() {
        // -- ARRANGE --
        final SupportIssueMessage originalMessage = TestSupportIssueMessages.getRandomValid();

        // attach to issue parent
        final SupportIssue currentIssue = issueRepository.findById(currentSupportIssueId);
        currentIssue.addMessage(originalMessage);

        // -- ACT --
        // pass message instance to the entity manager
        messageRepository.persist(originalMessage);
        entityManager.flush();

        // -- ASSERT --
        final SupportIssueMessage persistedMessage = messageRepository.findById(originalMessage.getId());
        assertEquals(originalMessage, persistedMessage);
    }

    @Test
    @DisplayName("creating a message without issue should fail")
    @Transactional
    public void createWithoutIssue() {
        // -- ARRANGE --
        final SupportIssueMessage originalMessage = TestSupportIssueMessages.getRandomValid();

        // -- ACT --
        // pass message instance to the entity manager
        messageRepository.persist(originalMessage);

        // -- ASSERT --
        // execute database operations (should fail)
        assertThrows(Exception.class, () -> entityManager.flush());
    }

    @Test
    @DisplayName("testing relation to attachments")
    @Transactional
    public void changeMessageAttachments() {
        // -- ARRANGE --
        final SupportIssueMessage originalMessage = TestSupportIssueMessages.getRandomValid();

        // attach to issue parent
        final SupportIssue currentIssue = issueRepository.findById(currentSupportIssueId);
        currentIssue.addMessage(originalMessage);

        // add attachments to message
        final int attachmentCount = 5;
        for (int i = 0; i < 5; i++) {
            final Attachment attachment = TestAttachments.getRandomValid();
            originalMessage.addAttachment(attachment);
        }

        messageRepository.persist(originalMessage);

        // write all entities to database
        entityManager.flush();

        // fetch created message again
        SupportIssueMessage persistedMessage = messageRepository.findById(originalMessage.getId());
        assertEquals(attachmentCount, persistedMessage.getAttachments().size());

        // -- ACT --
        // add another attachment
        final Attachment attachment = TestAttachments.getRandomValid();
        persistedMessage.addAttachment(attachment);
        entityManager.flush();

        // -- ASSERT --
        persistedMessage = messageRepository.findById(originalMessage.getId());
        assertEquals(attachmentCount + 1, persistedMessage.getAttachments().size());
    }
}
