package de.benevolo.customer.support.entities;

import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.testdata.TestCustomerFeedback;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
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
        supportIssueRepository.deleteAll();
        feedbackRepository.deleteAll();
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
        feedbackRepository.flush();

        final CustomerFeedback persistedCustomerFeedback = feedbackRepository.findById(originalFeedback.getId());
        Assertions.assertNotNull(persistedCustomerFeedback);
        Assertions.assertEquals(originalFeedback, persistedCustomerFeedback);
    }

    @Test
    @DisplayName("persisting without issue should fail")
    @Transactional
    public void createWithoutIssue() {
        final CustomerFeedback customerFeedback = TestCustomerFeedback.getRandomValid();

        Assertions.assertThrows(Exception.class, () -> feedbackRepository.persist(customerFeedback));
    }


}
