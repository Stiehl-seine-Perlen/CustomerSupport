package de.benevolo.customer.support.services;

import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueStatus;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@QuarkusTest
public class FinalizeIssueServiceTest {

    @Inject
    SupportIssueRepository issueRepository;


    @Inject
    EntityManager entityManager;

    @Inject
    FinalizeIssueService service;

    @BeforeEach
    @Transactional
    public void prepare() {
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

}
