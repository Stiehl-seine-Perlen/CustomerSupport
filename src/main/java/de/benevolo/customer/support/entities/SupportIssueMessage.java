package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * During the issue resolving process, the customer can communicate
 * with the support team and vice versa. An instance of this class represents
 * such a chat message.
 *
 * @author Daniel Mehlber
 */
@Entity
public class SupportIssueMessage {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "message must not be blank")
    private String message;

    @NotNull(message = "isFromCustomer must not be null")
    private Boolean isFromCustomer;

    @OneToMany(mappedBy = "message", fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Attachment> attachments = new HashSet<>();

    // @NotNull(message = "issue is null, but message needs an issue")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private SupportIssue issue;

    @JsonCreator
    public SupportIssueMessage(@JsonProperty(required = true) final String message,
                               @JsonProperty(required = true) final boolean isFromCustomer,
                               @JsonProperty(required = true) final Set<Attachment> attachments) {
        this.message = message;
        this.isFromCustomer = isFromCustomer;
        attachments.forEach(this::addAttachment);
        this.attachments = attachments;
    }

    protected SupportIssueMessage() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public Boolean isFromCustomer() {
        return isFromCustomer;
    }

    public void setFromCustomer(final Boolean fromCustomer) {
        isFromCustomer = fromCustomer;
    }

    public Set<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(final Set<Attachment> attachments) {
        this.attachments = attachments;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public SupportIssue getIssue() {
        return issue;
    }

    public void setIssue(final SupportIssue issue) {
        this.issue = issue;
    }

    public void addAttachment(final Attachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SupportIssueMessage that = (SupportIssueMessage) o;
        return Objects.equals(id, that.id) && Objects.equals(message, that.message) && Objects.equals(isFromCustomer, that.isFromCustomer) && Objects.equals(attachments, that.attachments) && Objects.equals(issue, that.issue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, isFromCustomer, attachments);
    }

    @Override
    public String toString() {
        return "SupportIssueMessage{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", isFromCustomer=" + isFromCustomer +
                ", attachments=" + attachments +
                ", issueId=" + issue.getId() +
                '}';
    }
}
