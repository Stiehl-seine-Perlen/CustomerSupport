package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testData.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.List;
import java.util.Set;

@QuarkusTest
public class SupportIssueTest {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    Validator validator;

    @BeforeEach
    @Transactional
    public void prepare() {
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("valid entities should pass")
    public void validateValid() {
        final List<SupportIssue> validIssues = TestSupportIssues.getAllValidCombinations();

        for (final SupportIssue validIssue : validIssues) {
            final Set<ConstraintViolation<SupportIssue>> violations = validator.validate(validIssue);
            Assertions.assertEquals(0, violations.size(), "violations occurred on valid entity");
        }
    }

    @Test
    @DisplayName("invalid entities should not pass")
    public void validateInvalid() {
        final List<SupportIssue> invalidIssues = TestSupportIssues.getAllInvalidCombinations();

        for (final SupportIssue invalidIssue : invalidIssues) {
            final Set<ConstraintViolation<SupportIssue>> violations = validator.validate(invalidIssue);
            Assertions.assertNotEquals(0, violations.size(), "no violations occurred on invalid entity: " + invalidIssue.toString());
        }
    }

    @Test
    @DisplayName("test entity CRUD operations")
    @Transactional
    public void crud() {
        final SupportIssue originalIssue = TestSupportIssues.getRandomValid();

        // test create
        issueRepository.persist(originalIssue);
        Assertions.assertTrue(issueRepository.isPersistent(originalIssue));

        // test read
        final SupportIssue persistedIssue = issueRepository.findById(originalIssue.getId()); // read
        Assertions.assertEquals(originalIssue, persistedIssue, "the read entity is different");

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
        Assertions.assertEquals(persistedIssue, updatedIssue);

        // test delete
        issueRepository.delete(updatedIssue);
        final boolean stillExists = issueRepository.findById(updatedIssue.getId()) != null;
        Assertions.assertFalse(stillExists);

    }

    @Test
    @DisplayName("test update messages of issue")
    @Transactional
    public void updateMessagesOfIssue() {
    }

}
