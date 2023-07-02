package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
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
public class SupportIssueMessage implements Comparable<SupportIssueMessage> {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank(message = "message must not be blank")
    private String message;

    /**
     * Defines who sent the message (either the customer or the support team)
     */
    @NotNull(message = "fromCustomer is missing")
    private Boolean fromCustomer;

    /**
     * Marks the message as the one, which resolved the issue. If this is true, the issue may have been closed.
     * Usually this message has been sent by the customer, but can also come from the support process in case the
     * customer wasn't replying or the issue cannot be solved.
     */
    @NotNull(message = "hasResolvedIssue is missing")
    private Boolean hasResolvedIssue;

    /**
     * All attachments of this message. This contains images, files and other additional content that could
     * help the support team resolving the issue.
     */
    @OneToMany(mappedBy = "message")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<Attachment> attachments = new HashSet<>();

    /**
     * No message can exist without its parent issue. If the parent issue is deleted, all its messages will be deleted
     * as well.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore
    private SupportIssue issue;

    /**
     * The creation timestamp marks the time of creation (obviously). It is used to sort all messages of an issue
     * in order to display them as chat.
     */
    private LocalDateTime creation;

    @PrePersist
    protected void persist() {
        creation = LocalDateTime.now();
    }

    @JsonCreator
    public SupportIssueMessage(@JsonProperty(required = true) final String message,
                               @JsonProperty(required = true) final Set<Attachment> attachments,
                               @JsonProperty(required = true) final Boolean hasResolvedIssue) {
        this.message = message;
        attachments.forEach(this::addAttachment);
        this.attachments = attachments;
        this.hasResolvedIssue = hasResolvedIssue;
        this.fromCustomer = false;
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
        return fromCustomer;
    }

    public void setFromCustomer(final Boolean fromCustomer) {
        this.fromCustomer = fromCustomer;
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

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(final LocalDateTime creation) {
        this.creation = creation;
    }

    public Boolean getHasResolvedIssue() {
        return hasResolvedIssue;
    }

    public void setHasResolvedIssue(final Boolean hasResolvedIssue) {
        this.hasResolvedIssue = hasResolvedIssue;
    }

    public void addAttachment(final Attachment attachment) {
        attachments.add(attachment);
        attachment.setMessage(this);
    }


    // ignores related entities (due to JPA) and only compares direct attributes.
    // and yes, its reflexive, symmetric and transitive
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SupportIssueMessage message1 = (SupportIssueMessage) o;
        return Objects.equals(id, message1.id) && Objects.equals(message, message1.message) && Objects.equals(fromCustomer, message1.fromCustomer) && Objects.equals(hasResolvedIssue, message1.hasResolvedIssue) && Objects.equals(creation, message1.creation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, message, fromCustomer, hasResolvedIssue, creation);
    }

    @Override
    public String toString() {
        return "SupportIssueMessage{" +
                "id=" + id +
                ", message='" + message + '\'' +
                ", isFromCustomer=" + fromCustomer +
                ", hasResolvedIssue=" + hasResolvedIssue +
                ", creation=" + creation +
                '}';
    }

    @Override
    public int compareTo(final SupportIssueMessage supportIssueMessage) {
        return creation.compareTo(supportIssueMessage.creation);
    }
}
