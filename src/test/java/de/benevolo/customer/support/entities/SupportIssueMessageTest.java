package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testData.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testData.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
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

    @Test
    @DisplayName("valid messages should pass")
    public void validateValid() {
        final List<SupportIssueMessage> validMessages = TestSupportIssueMessages.getAllValidCombinations();

        for (final SupportIssueMessage message : validMessages) {
            final Set<ConstraintViolation<SupportIssueMessage>> violations = validator.validate(message);
            Assertions.assertEquals(0, violations.size(), "there are violations in valid messages");
        }
    }

    @Test
    @DisplayName("invalid messages should not pass")
    public void validateInvalid() {
        final List<SupportIssueMessage> invalidMessages = TestSupportIssueMessages.getAllInvalidCombinations();

        for (final SupportIssueMessage message : invalidMessages) {
            final Set<ConstraintViolation<SupportIssueMessage>> violations = validator.validate(message);
            Assertions.assertNotEquals(0, violations.size(), "there are no violations in invalid messages");
        }
    }

    @Test
    @DisplayName("creating a message with issue should work")
    @Transactional
    public void createWithIssue() {
        final SupportIssueMessage originalMessage = TestSupportIssueMessages.getRandomValid();

        // attach to issue parent
        final SupportIssue currentIssue = issueRepository.findById(currentSupportIssueId);
        currentIssue.addMessage(originalMessage);

        // pass message instance to the entity manager
        messageRepository.persist(originalMessage);

        // execute database operations
        entityManager.flush();

        final SupportIssueMessage persistedMessage = messageRepository.findById(originalMessage.getId());
        Assertions.assertEquals(originalMessage, persistedMessage);
    }

    @Test
    @DisplayName("creating a message without issue should fail")
    @Transactional
    public void createWithoutIssue() {
        final SupportIssueMessage originalMessage = TestSupportIssueMessages.getRandomValid();

        // pass message instance to the entity manager
        messageRepository.persist(originalMessage);

        // execute database operations
        Assertions.assertThrows(Exception.class, () -> entityManager.flush());
    }

}
