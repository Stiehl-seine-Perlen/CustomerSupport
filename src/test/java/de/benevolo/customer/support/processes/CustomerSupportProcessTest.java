package de.benevolo.customer.support.processes;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.*;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import org.kie.kogito.process.WorkItem;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.*;

@QuarkusTest
public class CustomerSupportProcessTest {

    @Inject
    @Named("CustomerSupport")
    Process<? extends Model> customerSupportProcess;

    @Inject
    @Named("ResolveSupportIssue")
    Process<? extends Model> resolveIssueProcess;

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    private Set<UUID> currentAttachmentIds = new HashSet<>();

    private static final int attachmentCount = 5;

    @BeforeEach
    @Transactional
    public void prepare() {
        issueRepository.deleteAll();
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();

        // fill database with attachments that can be used in the next test
        currentAttachmentIds = new HashSet<>();
        for (int i = 0; i < attachmentCount; i++) {
            final Attachment attachment = TestAttachments.getRandomValid();
            attachmentRepository.persist(attachment);
            currentAttachmentIds.add(attachment.getId());
        }
    }

    private Map<String, Object> generateValidParameters() {
        final SupportIssue issue = TestSupportIssues.getRandomValid();
        final SupportIssueMessage message = TestSupportIssueMessages.getRandomValid();

        final SupportRequest request = new SupportRequest(issue.getTitle(), message.getMessage(), issue.getIssuerEmailAddress(), currentAttachmentIds);

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("request", request);

        return parameters;
    }

    @Transactional
    private List<SupportIssue> fetchAllSupportIssues() {
        final List<SupportIssue> issues = issueRepository.listAll();

        // trigger eager loading
        for (final SupportIssue issue : issues) {
            for (final SupportIssueMessage message : issue.getMessages()) {
                message.getAttachments();
            }
        }

        return issues;
    }

    @Test
    @Transactional
    public void happyPath() {
        final ProcessInstance<?> customerSupportInstance = startBySendingRequest();

        final ProcessInstance<?> resolveIssueInstance = resolveIssueProcess.instances().stream().findFirst().orElse(null);
        performAnswerByCustomerSupport(resolveIssueInstance);
    }

    private ProcessInstance<?> startBySendingRequest() {
        Assertions.assertNotNull(customerSupportProcess);

        final Map<String, Object> parameters = generateValidParameters();
        final Model model = customerSupportProcess.createModel();
        model.fromMap(parameters);

        final ProcessInstance<?> instance = customerSupportProcess.createInstance(model);
        instance.start();

        final SupportRequest sentRequest = (SupportRequest) parameters.get("request");

        final List<SupportIssue> issues = fetchAllSupportIssues();
        Assertions.assertNotNull(issues);
        Assertions.assertEquals(1, issues.size(), "1 issue should have been created");

        final SupportIssue createdIssue = issues.get(0);
        Assertions.assertEquals(sentRequest.getIssuerEmailAddress(), createdIssue.getIssuerEmailAddress(), "wrong issuer email");
        Assertions.assertEquals(sentRequest.getTitle(), createdIssue.getTitle(), "wrong title");
        Assertions.assertEquals(SupportIssueStatus.OPEN, createdIssue.getStatus());
        Assertions.assertEquals(1, createdIssue.getMessages().size(), "1 message should be in issue");

        final SupportIssueMessage message = createdIssue.getMessages().stream().findFirst().orElse(null);
        Assertions.assertNotNull(message);
        Assertions.assertEquals(sentRequest.getMessage(), message.getMessage(), "wrong message");
        Assertions.assertEquals(attachmentCount, message.getAttachments().size(), "wrong count of attachments in first message");

        final long incorrectIdsCount = message.getAttachments().stream()
                .map(Attachment::getId)
                .filter(id -> !currentAttachmentIds.contains(id))
                .count();

        Assertions.assertEquals(0, incorrectIdsCount);

        return instance;
    }

    private void performAnswerByCustomerSupport(final ProcessInstance<?> resolveProcessInstance) {
        Assertions.assertNotNull(resolveProcessInstance);

        final WorkItem userTask = resolveProcessInstance.workItems().stream().findFirst().orElse(null);
        Assertions.assertNotNull(userTask);
        Assertions.assertEquals("WriteReplyToCustomer", userTask.getName());

        final String userTaskId = userTask.getId();

        final Map<String, Object> parameters = new HashMap<>();
        final SupportIssueMessage replyToCustomer = TestSupportIssueMessages.getRandomValid();
        parameters.put("message", replyToCustomer);

        final long messageCountBeforeAnswer = fetchAllSupportIssues().size();
        resolveProcessInstance.completeWorkItem(userTaskId, parameters);

        final SupportIssue currentIssue = fetchAllSupportIssues().stream().findFirst().orElse(null);
        Assertions.assertNotNull(currentIssue);
        Assertions.assertEquals(SupportIssueStatus.IN_WORK, currentIssue.getStatus(), "issue has wrong status");

        final Set<SupportIssueMessage> messages = currentIssue.getMessages();
        Assertions.assertEquals(messageCountBeforeAnswer + 1, messages.size(), "message has not been added");
    }

}
