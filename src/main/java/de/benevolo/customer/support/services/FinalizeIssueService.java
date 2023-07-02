package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.CustomerFeedback;
import de.benevolo.customer.support.entities.SupportIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class FinalizeIssueService {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    CustomerFeedbackRepository feedbackRepository;

    private static final Logger LOG = LoggerFactory.getLogger(FinalizeIssueService.class);

    @Transactional
    public void processCustomerFeedback(final Long issueId, final CustomerFeedback feedback) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.setFeedback(feedback);

        feedbackRepository.persist(feedback);

    }

    /**
     * Mark {@link SupportIssue} as "closed". This updates the state of the issue.
     *
     * @param issueId id of the {@link SupportIssue}
     * @author Daniel Mehlber
     */
    @Transactional
    public void closeSupportIssue(final Long issueId) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.complete();
        LOG.info("closed support issue id: {}", issueId);
    }

    public String generateCustomerConfirmation(final Long issueId) {
        LOG.info("generated issue closing confirmation for customer");
        return String.format("the issue with id:%d was closed. Thanks you for using Benevolo!", issueId);
    }

}
