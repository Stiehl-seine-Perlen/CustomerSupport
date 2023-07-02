package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.SupportIssueRepository;
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
public class SupportIssueTest {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    Validator validator;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    public void prepare() {
        issueRepository.deleteAll();
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("valid entities should pass")
    public void validateValid() {
        final List<SupportIssue> validIssues = TestSupportIssues.getAllValidCombinations();

        for (final SupportIssue validIssue : validIssues) {
            final Set<ConstraintViolation<SupportIssue>> violations = validator.validate(validIssue);
            assertEquals(0, violations.size(), "violations occurred on valid entity");
        }
    }

    @Test
    @DisplayName("invalid entities should not pass")
    public void validateInvalid() {
        final List<SupportIssue> invalidIssues = TestSupportIssues.getAllInvalidCombinations();

        for (final SupportIssue invalidIssue : invalidIssues) {
            final Set<ConstraintViolation<SupportIssue>> violations = validator.validate(invalidIssue);
            assertNotEquals(0, violations.size(), "no violations occurred on invalid entity: " + invalidIssue.toString());
        }
    }

    @Test
    @DisplayName("test entity CRUD operations")
    @Transactional
    public void crud() {
        // -- ARRANGE --
        final SupportIssue originalIssue = TestSupportIssues.getRandomValid();

        // -- ACT & ASSERT --
        // test create
        issueRepository.persist(originalIssue);
        assertTrue(issueRepository.isPersistent(originalIssue));

        // test read
        final SupportIssue persistedIssue = issueRepository.findById(originalIssue.getId()); // read
        assertEquals(originalIssue, persistedIssue, "the read entity is different");

        // test update
        issueRepository.persist(persistedIssue);
        final String originalTitle = persistedIssue.getTitle();
        final String originalEmail = persistedIssue.getIssuerEmailAddress();

        // find different values for issue
        do {
            persistedIssue.setTitle(TestSupportIssues.getRandomValid().getTitle());
            persistedIssue.setIssuerEmailAddress(TestSupportIssues.getRandomValid().getIssuerEmailAddress());
        } while (originalTitle.equals(persistedIssue.getTitle()) ||
                originalEmail.equals(persistedIssue.getIssuerEmailAddress()));

        issueRepository.persist(persistedIssue);

        final SupportIssue updatedIssue = issueRepository.findById(persistedIssue.getId());
        assertEquals(persistedIssue, updatedIssue);

        // test delete
        issueRepository.delete(updatedIssue);
        final boolean stillExists = issueRepository.findById(updatedIssue.getId()) != null;
        assertFalse(stillExists);

    }

    @Test
    @DisplayName("test update messages of issue")
    @Transactional
    public void updateMessagesOfIssue() {
        // -- ARRANGE --
        // build an issue with messages in it
        final SupportIssue originalIssue = TestSupportIssues.getRandomValid();
        final int messageCount = 5;
        for (int i = 0; i < messageCount; i++) {
            final SupportIssueMessage message = TestSupportIssueMessages.getRandomValid();
            originalIssue.addMessage(message);
        }

        issueRepository.persist(originalIssue);

        // store original issue incl. message to database
        entityManager.flush();

        // -- ACT --
        // modify the messages of the issue
        final SupportIssue fetchedIssue = issueRepository.findById(originalIssue.getId());
        assertEquals(messageCount, fetchedIssue.getMessages().size());
        fetchedIssue.addMessage(TestSupportIssueMessages.getRandomValid());

        // store new issue incl. messages to database
        entityManager.flush();

        // -- ASSERT --
        // check if changes have been persisted on database correctly
        final SupportIssue modifiedIssue = issueRepository.findById(originalIssue.getId());
        assertEquals(messageCount + 1, modifiedIssue.getMessages().size());
    }

}
