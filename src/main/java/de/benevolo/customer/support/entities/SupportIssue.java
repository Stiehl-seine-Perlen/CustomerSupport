package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
public class SupportIssue {

    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    @Email
    private String issuerEmailAddress;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SupportIssueStatus status;

    @OneToMany(mappedBy = "issue", orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<SupportIssueMessage> messages = new HashSet<>();

    protected SupportIssue() {
    }

    @JsonCreator
    public SupportIssue(@JsonProperty(required = true) final String title,
                        @JsonProperty(required = true) final String issuerEmailAddress,
                        @JsonProperty(required = true) final SupportIssueStatus status) {
        this.title = title;
        this.issuerEmailAddress = issuerEmailAddress;
        this.status = status;
    }

    public SupportIssue(final SupportRequest request) {
        this.title = request.getTitle();
        this.issuerEmailAddress = request.getIssuerEmailAddress();
        this.status = SupportIssueStatus.OPEN;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getIssuerEmailAddress() {
        return issuerEmailAddress;
    }

    public void setIssuerEmailAddress(final String issuerEmailAddress) {
        this.issuerEmailAddress = issuerEmailAddress;
    }

    public Set<SupportIssueMessage> getMessages() {
        return messages;
    }

    public void setMessages(final Set<SupportIssueMessage> messages) {
        this.messages = messages;
    }

    public SupportIssueStatus getStatus() {
        return status;
    }

    public void setStatus(final SupportIssueStatus status) {
        this.status = status;
    }

    public void addMessage(final SupportIssueMessage message) {
        messages.add(message);
        message.setIssue(this);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SupportIssue that = (SupportIssue) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(issuerEmailAddress, that.issuerEmailAddress) && status == that.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, issuerEmailAddress, status);
    }

    @Override
    public String toString() {
        return "SupportIssue{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", issuerEmailAddress='" + issuerEmailAddress + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}