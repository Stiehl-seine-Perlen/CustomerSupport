package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testdata.TestCustomerFeedback;
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
            Assertions.assertEquals(0, violations.size(), "violations found on valid feedback");
        }
    }

    @Test
    @DisplayName("CRUD operations should work")
    @Transactional
    public void crud() {
        final CustomerFeedback originalFeedback = TestCustomerFeedback.getRandomValid();

        final SupportIssue issue = supportIssueRepository.findById(currentIssueId);
        issue.setFeedback(originalFeedback);

        feedbackRepository.persist(originalFeedback);
        entityManager.flush();

        final CustomerFeedback persistedCustomerFeedback = feedbackRepository.findById(originalFeedback.getId());
        Assertions.assertNotNull(persistedCustomerFeedback);
        Assertions.assertEquals(originalFeedback, persistedCustomerFeedback);
    }

    @Test
    @DisplayName("deleting the issue should delete the feedback as well")
    @Transactional
    public void cascadeDelete() {
        // create using issue
        final CustomerFeedback originalFeedback = TestCustomerFeedback.getRandomValid();

        final SupportIssue issue = supportIssueRepository.findById(currentIssueId);
        issue.setFeedback(originalFeedback);

        feedbackRepository.persist(originalFeedback);
        entityManager.flush();

        // delete issue
        supportIssueRepository.deleteById(currentIssueId);
        entityManager.flush();

        // check if feedback has been deleted as well
        Assertions.assertNull(feedbackRepository.findById(originalFeedback.getId()));
    }


}
