package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.Attachment;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueMessage;
import de.benevolo.customer.support.entities.SupportRequest;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Validator;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class PrepareIssueService {

    @Inject
    Logger log;

    @Inject
    SupportIssueRepository supportIssueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    Validator validator;


    /**
     * Creates a new support issue from a support request and also generate the first message by the customer.
     *
     * @param request the request which will be converted to the support issue and its content
     * @return the id of the created support issue
     * @author Daniel Mehlber
     */
    @Transactional
    public Long createSupportIssue(final SupportRequest request) {
        // create request from issue (without message or attachments)
        final SupportIssue issue = new SupportIssue(request);
        log.debugf("created new support issue with title '%s'", issue.getTitle());

        // convert attachment ids to attachments
        final Set<Attachment> firstMessageAttachments = fetchAttachmentsByIds(request.getAttachmentsIds());

        // create the first message of the issue using attachments and the request message
        final SupportIssueMessage firstMessage = new SupportIssueMessage(request.getMessage(), firstMessageAttachments, false);
        firstMessage.setFromCustomer(true);
        log.debugf("created first support issue message with %d attachments", firstMessageAttachments.size());

        // attach first message to new issue
        issue.addMessage(firstMessage);

        // persist this
        supportIssueRepository.persist(issue);
        messageRepository.persist(firstMessage);

        log.infof("created new support issue id:%d from request", issue.getId());

        return issue.getId();
    }

    @Transactional
    Set<Attachment> fetchAttachmentsByIds(final Set<UUID> attachmentsIds) {
        log.debugf("fetching %d attachments by their ids", attachmentsIds.size());

        final Set<Attachment> attachments = attachmentsIds.stream()
                .map(id -> attachmentRepository.findById(id))
                .filter(Objects::nonNull) // if an attachment was not found, ignore it
                .collect(Collectors.toSet());

        if (attachments.size() != attachmentsIds.size()) {
            log.warnf("some attachments were not uploaded correctly: found %d ids in request, but only %d matching attachments",
                    attachmentsIds.size(), attachments.size());
        }

        return attachments;
    }

    public String generateCustomerNotificationEmail(final SupportRequest request) {
        return String.format("we will work on your request with title '%s'", request.getTitle());
    }

    public String generateSupportTeamNotification(final SupportRequest request) {
        return String.format("we have a new issue named '%s'", request.getTitle());
    }
}
