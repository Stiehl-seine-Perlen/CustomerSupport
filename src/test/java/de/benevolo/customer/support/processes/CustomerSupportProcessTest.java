package de.benevolo.customer.support.processes;

import de.benevolo.customer.support.database.AttachmentRepository;
import de.benevolo.customer.support.database.CustomerFeedbackRepository;
import de.benevolo.customer.support.database.SupportIssueMessageRepository;
import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.*;
import de.benevolo.customer.support.entities.testdata.TestAttachments;
import de.benevolo.customer.support.entities.testdata.TestCustomerFeedback;
import de.benevolo.customer.support.entities.testdata.TestSupportIssueMessages;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;
import org.kie.kogito.Model;
import org.kie.kogito.internal.process.runtime.KogitoNode;
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
    @Named("FinalizeSupportIssue")
    Process<? extends Model> finalizeIssueProcess;

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    SupportIssueMessageRepository messageRepository;

    @Inject
    AttachmentRepository attachmentRepository;

    @Inject
    CustomerFeedbackRepository feedbackRepository;

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

    @AfterEach
    @Transactional
    public void cleanUp() {
        feedbackRepository.deleteAll();
        issueRepository.deleteAll();
        attachmentRepository.deleteAll();
        messageRepository.deleteAll();
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
    @DisplayName("the happy path should work")
    @Transactional
    public void happyPath() {
        final ProcessInstance<?> customerSupportInstance = startBySendingRequest();
        final ProcessInstance<?> resolveIssueInstance = resolveIssueProcess.instances().stream().findFirst().orElse(null);
        Assertions.assertNotNull(resolveIssueInstance);

        // perform chat between customer and support team
        performAnswerBySupportTeam(resolveIssueInstance);
        performAnswerByCustomer(resolveIssueInstance, false);
        performAnswerBySupportTeam(resolveIssueInstance);
        performAnswerByCustomer(resolveIssueInstance, true);

        Assertions.assertEquals(ProcessInstance.STATE_COMPLETED, resolveIssueInstance.status());

        // finalize issue
        final ProcessInstance<?> finalizeIssueInstance = finalizeIssueProcess.instances().stream().findFirst().orElse(null);
        performCustomerFeedback(finalizeIssueInstance);

        Assertions.assertEquals(ProcessInstance.STATE_COMPLETED, finalizeIssueInstance.status());
        Assertions.assertEquals(ProcessInstance.STATE_COMPLETED, customerSupportInstance.status());
    }

    private ProcessInstance<?> startBySendingRequest() {
        Assertions.assertNotNull(customerSupportProcess);

        // prepare parameters and process model
        final Map<String, Object> parameters = generateValidParameters();
        final Model model = customerSupportProcess.createModel();
        model.fromMap(parameters);

        // start process using parameters
        final ProcessInstance<?> instance = customerSupportProcess.createInstance(model);
        instance.start();

        // assertions
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

    private void performAnswerBySupportTeam(final ProcessInstance<?> resolveProcessInstance) {
        Assertions.assertNotNull(resolveProcessInstance);

        triggerWriteReplyToCustomerTask(resolveProcessInstance);
        final String currentUserTaskId = findCurrentUserTask(resolveProcessInstance, "WriteReplyToCustomer");

        // prepare answer by support team
        final Map<String, Object> parameters = new HashMap<>();
        final SupportIssueMessage replyToCustomer = TestSupportIssueMessages.getRandomValid();
        parameters.put("message", replyToCustomer);

        // send reply by support team
        final SupportIssue currentIssue = fetchAllSupportIssues().stream().findFirst().orElse(null);
        Assertions.assertNotNull(currentIssue);
        final long messageCountBeforeAnswer = currentIssue.getMessages().size();
        resolveProcessInstance.completeWorkItem(currentUserTaskId, parameters);

        // assertions
        Assertions.assertEquals(SupportIssueStatus.IN_WORK, currentIssue.getStatus(), "issue has wrong status");

        final List<SupportIssueMessage> messages = currentIssue.getMessages();
        Assertions.assertEquals(messageCountBeforeAnswer + 1, messages.size(), "message has not been added");

        final SupportIssueMessage latestMessage = extractLatestMessage(messages);
        Assertions.assertEquals(replyToCustomer, latestMessage);
        Assertions.assertEquals(false, latestMessage.isFromCustomer(), "message must be from support team, not customer");
    }

    private void performAnswerByCustomer(final ProcessInstance<?> resolveProcessInstance, final boolean messageResolvesIssue) {
        Assertions.assertNotNull(resolveProcessInstance);

        // trigger and get current user task
        triggerWriteReplyToSupportTask(resolveProcessInstance);
        final String currentUserTaskId = findCurrentUserTask(resolveProcessInstance, "WriteReplyToSupport");


        // prepare answer by customer
        final Map<String, Object> parameters = new HashMap<>();
        final SupportIssueMessage replyToCustomer = TestSupportIssueMessages.getRandomValid();
        replyToCustomer.setHasResolvedIssue(messageResolvesIssue);
        parameters.put("message", replyToCustomer);

        // send answer
        final SupportIssue currentIssue = fetchAllSupportIssues().stream().findFirst().orElse(null);
        Assertions.assertNotNull(currentIssue);
        final long messageCountBeforeAnswer = currentIssue.getMessages().size();
        resolveProcessInstance.completeWorkItem(currentUserTaskId, parameters);

        // assertions
        if (messageResolvesIssue) {
            Assertions.assertEquals(SupportIssueStatus.CLOSED, currentIssue.getStatus(), "issue has wrong status");
        } else {
            Assertions.assertEquals(SupportIssueStatus.IN_WORK, currentIssue.getStatus(), "issue has wrong status");
        }

        final List<SupportIssueMessage> messages = currentIssue.getMessages();
        Assertions.assertEquals(messageCountBeforeAnswer + 1, messages.size(), "message has not been added");

        final SupportIssueMessage latestMessage = extractLatestMessage(messages);
        Assertions.assertEquals(replyToCustomer, latestMessage);
        Assertions.assertEquals(true, latestMessage.isFromCustomer(), "message must be from support team, not customer");
    }

    private void performCustomerFeedback(final ProcessInstance<?> finalizeIssueInstance) {
        Assertions.assertNotNull(finalizeIssueInstance);

        // get current user task
        final String currentUserTaskId = findCurrentUserTask(finalizeIssueInstance, "Feedback");

        // prepare parameters
        final Map<String, Object> parameters = new HashMap<>();
        final CustomerFeedback customerFeedback = TestCustomerFeedback.getRandomValid();
        parameters.put("feedback", customerFeedback);

        // send feedback
        finalizeIssueInstance.completeWorkItem(currentUserTaskId, parameters);
    }


    private void triggerWriteReplyToSupportTask(final ProcessInstance<?> resolveIssueInstance) {
        // I added additional metadata to the user task node (in the BPMN editor) in order to identify the node here
        final KogitoNode taskNode = resolveIssueInstance.process().findNodes(node -> "WriteReplyToSupport".equals(node.getMetaData().get("taskName")))
                .stream().findFirst().orElse(null);

        Assertions.assertNotNull(taskNode);
        resolveIssueInstance.triggerNode(taskNode.getNodeUniqueId());
    }

    private void triggerWriteReplyToCustomerTask(final ProcessInstance<?> resolveIssueInstance) {
        // I added additional metadata to the user task node (in the BPMN editor) in order to identify the node here
        final KogitoNode taskNode = resolveIssueInstance.process().findNodes(node -> "WriteReplyToCustomer".equals(node.getMetaData().get("taskName")))
                .stream().findFirst().orElse(null);

        Assertions.assertNotNull(taskNode);
        resolveIssueInstance.triggerNode(taskNode.getNodeUniqueId());
    }

    private String findCurrentUserTask(final ProcessInstance<?> instance, final String name) {
        final WorkItem currentUserTask = instance.workItems().stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
        Assertions.assertNotNull(currentUserTask);
        Assertions.assertEquals(name, currentUserTask.getName(), "current user task should be " + name);
        return currentUserTask.getId();
    }

    private SupportIssueMessage extractLatestMessage(final Collection<SupportIssueMessage> messages) {
        return messages.stream().toList()
                .stream().sorted().reduce((first, second) -> second).orElse(null);
    }
}