package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

@Entity
public class SupportIssue {

    @Id
    private Long id;

    @NotNull
    private String title;

    @NotNull
    @Email
    private String issuerEmailAddress;

    @OneToMany(mappedBy = "issue")
    private Set<SupportIssueMessage> messages;

    protected SupportIssue() {
    }

    @JsonCreator
    public SupportIssue(@JsonProperty(required = true) final String title,
                        @JsonProperty(required = true) final String issuerEmailAddress) {
        this.title = title;
        this.issuerEmailAddress = issuerEmailAddress;
    }

    public SupportIssue(final SupportRequest request) {
        this.title = request.getTitle();
        this.issuerEmailAddress = request.getIssuerEmailAddress();
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SupportIssue that = (SupportIssue) o;
        return Objects.equals(id, that.id) && Objects.equals(title, that.title) && Objects.equals(issuerEmailAddress, that.issuerEmailAddress) && Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, issuerEmailAddress, messages);
    }
}
