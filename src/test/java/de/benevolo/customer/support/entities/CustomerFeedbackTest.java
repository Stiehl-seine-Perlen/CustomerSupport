package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testdata.TestCustomerFeedback;
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
public class CustomerFeedbackTest {

    @Inject
    CustomerFeedbackRepository feedbackRepository;

    @Inject
    SupportIssueRepository supportIssueRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    Validator validator;

    private Long currentIssueId;

    @BeforeEach
    @Transactional
    public void prepare() {
        feedbackRepository.deleteAll();
        supportIssueRepository.deleteAll();

        final SupportIssue issue = TestSupportIssues.getRandomValid();
        supportIssueRepository.persist(issue);
        currentIssueId = issue.getId();
    }


    @AfterEach
    @Transactional
    public void cleanUp() {
        feedbackRepository.deleteAll();
        supportIssueRepository.deleteAll();
    }

    @Test
    @DisplayName("valid entities should pass")
    public void validateValid() {
        final List<CustomerFeedback> feedbacks = TestCustomerFeedback.getAllValidCombinations();

        for (final CustomerFeedback feedback : feedbacks) {
            final Set<ConstraintViolation<CustomerFeedback>> violations = validator.validate(feedback);
            assertEquals(0, violations.size(), "violations found on valid feedback");
        }
    }

    @Test
    @DisplayName("CRUD operations should work")
    @Transactional
    public void crud() {
        // -- ARRANGE --
        final CustomerFeedback originalFeedback = TestCustomerFeedback.getRandomValid();

        // -- ACT --
        final SupportIssue issue = supportIssueRepository.findById(currentIssueId);
        issue.setFeedback(originalFeedback);

        feedbackRepository.persist(originalFeedback);
        entityManager.flush();

        final CustomerFeedback persistedCustomerFeedback = feedbackRepository.findById(originalFeedback.getId());

        // -- ASSERT --
        assertNotNull(persistedCustomerFeedback);
        assertEquals(originalFeedback, persistedCustomerFeedback);
    }

    @Test
    @DisplayName("deleting the issue should delete the feedback as well")
    @Transactional
    public void cascadeDelete() {
        // -- ARRANGE --
        // create using issue
        final CustomerFeedback originalFeedback = TestCustomerFeedback.getRandomValid();

        final SupportIssue issue = supportIssueRepository.findById(currentIssueId);
        issue.setFeedback(originalFeedback);

        feedbackRepository.persist(originalFeedback);
        entityManager.flush();

        // -- ACT --
        // delete issue
        supportIssueRepository.deleteById(currentIssueId);
        entityManager.flush();

        // -- ASSERT --
        // check if feedback has been deleted as well
        assertNull(feedbackRepository.findById(originalFeedback.getId()));
    }


}
