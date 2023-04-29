package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueMessage;
import de.benevolo.customer.support.entities.SupportIssueStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ResolveIssueService {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;


    /**
     * Mark {@link SupportIssue} as "in work". This updates the issues state.
     *
     * @param issueId id of {@link SupportIssue}
     * @author Daniel Mehlber
     */
    @Transactional
    public void issueInWork(final Long issueId) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.setStatus(SupportIssueStatus.IN_WORK);
    }

    @Transactional
    public Long addSupportMessageToIssue(final Long issueId, final SupportIssueMessage message) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.addMessage(message);

        message.setFromCustomer(false);
        messageRepository.persist(message);

        return message.getId();
    }

    @Transactional
    public Long addCustomerMessageToIssue(final Long issueId, final SupportIssueMessage message) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.addMessage(message);

        message.setFromCustomer(true);
        messageRepository.persist(message);

        return message.getId();
    }

    public Boolean processCustomerReply(final Long issueId, final Long messageId) {
        final SupportIssueMessage message = messageRepository.findById(messageId);

        return false;
    }

}
