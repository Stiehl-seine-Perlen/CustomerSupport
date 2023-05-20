package de.benevolo.customer.support.services;

import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.CustomerFeedback;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueStatus;
import de.benevolo.customer.support.entities.testdata.TestCustomerFeedback;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@QuarkusTest
public class FinalizeIssueServiceTest {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    CustomerFeedbackRepository feedbackRepository;

    @Inject
    EntityManager entityManager;

    @Inject
    FinalizeIssueService service;

    @BeforeEach
    @Transactional
    public void prepare() {
        feedbackRepository.deleteAll();
        issueRepository.deleteAll();
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        feedbackRepository.deleteAll();
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("should close issue")
    @Transactional
    public void closeIssue() {
        final SupportIssue issue = TestSupportIssues.getRandomValid();
        issue.setStatus(SupportIssueStatus.IN_WORK);

        issueRepository.persist(issue);
        issueRepository.flush();

        service.closeSupportIssue(issue.getId());

        final SupportIssue persistedIssue = issueRepository.findById(issue.getId());
        Assertions.assertEquals(SupportIssueStatus.CLOSED, persistedIssue.getStatus());
    }

    @Test
    @DisplayName("should process customer feedback")
    @Transactional
    public void processCustomerFeedback() {
        SupportIssue issue = TestSupportIssues.getRandomValid();
        issueRepository.persist(issue);

        entityManager.flush();

        final CustomerFeedback feedback = TestCustomerFeedback.getRandomValid();
        service.processCustomerFeedback(issue.getId(), feedback);

        issue = issueRepository.findById(issue.getId());
        final CustomerFeedback persistedFeedback = issue.getFeedback();
        Assertions.assertNotNull(persistedFeedback);
        Assertions.assertEquals(feedback, persistedFeedback);
    }

}
