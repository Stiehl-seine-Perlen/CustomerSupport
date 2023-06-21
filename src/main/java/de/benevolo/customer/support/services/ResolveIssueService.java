package de.benevolo.customer.support.services;


import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.Attachment;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueMessage;
import org.kie.kogito.internal.process.runtime.KogitoProcessContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;

@ApplicationScoped
public class ResolveIssueService {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    private static final Logger LOG = LoggerFactory.getLogger(ResolveIssueService.class);


    /**
     * Mark {@link SupportIssue} as "in work". This updates the issues state.
     *
     * @param issueId id of {@link SupportIssue}
     * @author Daniel Mehlber
     */
    @Transactional
    public void issueInWork(final Long issueId) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.inWork();

        LOG.info("marked issue id:{} as 'in-work'", issueId);
    }

    @Transactional
    private Set<Attachment> fetchExistingAttachments(final Set<Attachment> attachments) {
        final Set<Attachment> fetchedAttachments = new HashSet<>();
        for (final Attachment attachment : attachments) {
            final Attachment fetchedAttachment = attachmentRepository.findById(attachment.getId());
            if (fetchedAttachment != null) {
                fetchedAttachments.add(fetchedAttachment);
            }
        }

        return fetchedAttachments;
    }

    @Transactional
    public Long addSupportMessageToIssue(final Long issueId, final SupportIssueMessage message) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.addMessage(message);

        message.setFromCustomer(false);
        messageRepository.persist(message);

        final Set<Attachment> attachments = fetchExistingAttachments(message.getAttachments());
        for (final Attachment attachment : attachments) {
            message.addAttachment(attachment);
        }

        if (message.getAttachments().size() > attachments.size()) {
            LOG.warn("not all attachments were uploaded successfully: only accepted {} of {} attachments",
                    attachments.size(),
                    message.getAttachments().size());
        }

        LOG.info("added message by support team id:{} to support issue id:{}", message.getId(), issueId);

        return message.getId();
    }

    @Transactional
    public Long addCustomerMessageToIssue(final Long issueId, final SupportIssueMessage message) {
        final SupportIssue issue = issueRepository.findById(issueId);
        issue.addMessage(message);

        message.setFromCustomer(true);
        messageRepository.persist(message);

        final Set<Attachment> attachments = fetchExistingAttachments(message.getAttachments());
        for (final Attachment attachment : attachments) {
            message.addAttachment(attachment);
        }

        if (message.getAttachments().size() > attachments.size()) {
            LOG.warn("not all attachments were uploaded successfully: only accepted {} of {} attachments",
                    attachments.size(),
                    message.getAttachments().size());
        }

        LOG.info("added message id:{} by customer to support issue id:{}", message.getId(), issueId);

        return message.getId();
    }

    @Transactional
    public Boolean processCustomerReply(final Long issueId, final Long messageId) {
        LOG.info("processing message id:{} from customer of issue id:{}", messageId, issueId);
        final SupportIssueMessage message = messageRepository.findById(messageId);
        return message.getHasResolvedIssue();
    }

    @Transactional
    public void prepareIssue(final Long issueId, final KogitoProcessContext context) {
        final String resolveIssueProcessId = context.getProcessInstance().getStringId();

        final SupportIssue issue = issueRepository.findById(issueId);
        issue.setResolveIssueProcessId(resolveIssueProcessId);
    }

}
