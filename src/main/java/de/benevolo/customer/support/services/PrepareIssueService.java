package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.Attachment;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueMessage;
import de.benevolo.customer.support.entities.SupportRequest;
import de.benevolo.email.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(PrepareIssueService.class);

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
        issue.open();
        LOG.debug("created new support issue with title '{}'", issue.getTitle());

        // convert attachment ids to attachments
        final Set<Attachment> firstMessageAttachments = fetchAttachmentsByIds(request.getAttachmentsIds());

        // create the first message of the issue using attachments and the request message
        final SupportIssueMessage firstMessage = new SupportIssueMessage(request.getMessage(), firstMessageAttachments, false);
        firstMessage.setFromCustomer(true);
        LOG.debug("created first support issue message with {} attachments", firstMessageAttachments.size());

        // attach first message to new issue
        issue.addMessage(firstMessage);

        // persist this
        supportIssueRepository.persist(issue);
        messageRepository.persist(firstMessage);

        LOG.info("created new support issue id:{} from request", issue.getId());

        return issue.getId();
    }

    @Transactional
    Set<Attachment> fetchAttachmentsByIds(final Set<UUID> attachmentsIds) {
        LOG.debug("fetching {} attachments by their ids", attachmentsIds.size());

        final Set<Attachment> attachments = attachmentsIds.stream()
                .map(id -> attachmentRepository.findById(id))
                .filter(Objects::nonNull) // if an attachment was not found, ignore it
                .collect(Collectors.toSet());

        if (attachments.size() != attachmentsIds.size()) {
            LOG.warn("some attachments were not uploaded correctly: found {} ids in request, but only {} matching attachments",
                    attachmentsIds.size(), attachments.size());
        }

        return attachments;
    }

    public Email generateCustomerNotificationEmail(final SupportRequest request, final Long issueId) {
        final String content = String.format("Ihre Anfrage wird bearbeitet. Sie können den Fortschritt <a href='dev.benevolo.de/support-issue/%d/customer'>hier</a> einsehen",
                issueId);

        return new Email(
                request.getIssuerEmailAddress(),
                request.getIssuerEmailAddress(),
                "Ihr Anliegen wird bearbeitet",
                content,
                "jan.vorhoff@outlook.de",
                "Jan Vorhoff");
    }

    public String generateSupportTeamNotification(final SupportRequest request) {
        return String.format("we have a new issue named '%s'", request.getTitle());
    }
}
