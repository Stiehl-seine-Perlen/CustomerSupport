package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueStatus;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;

@ApplicationScoped
public class ResolveIssueService {

    @Inject
    SupportIssueRepository issueRepository;


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

}
