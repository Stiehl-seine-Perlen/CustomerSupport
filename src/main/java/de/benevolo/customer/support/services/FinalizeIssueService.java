package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueStatus;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class FinalizeIssueService {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    Logger log;

    public void processCustomerFeedback() {
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
        issue.setStatus(SupportIssueStatus.CLOSED);
        log.infof("closed support issue id:%d", issueId);
    }

    public String generateCustomerConfirmation(final Long issueId) {
        log.info("generated issue closing confirmation for customer");
        return String.format("the issue with id:%d was closed. Thanks you for using Benevolo!", issueId);
    }

}
