package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

/**
 * This is sent when a user wants to open a new support issue
 */
public class SupportRequest {

    @NotBlank(message = "title must not be empty")
    private String title;

    @NotBlank(message = "summary must not be empty")
    private String message;

    @NotBlank(message = "the issuers email address may not be blank")
    @Email(message = "the issuers email address must be a valid email address")
    private String issuerEmailAddress;

    @NotNull(message = "attachments may be empty, but not null")
    private Set<UUID> attachmentsIds;

    protected SupportRequest() {
    }

    @JsonCreator
    public SupportRequest(@JsonProperty(required = true) final String title,
                          @JsonProperty(required = true) final String message,
                          @JsonProperty(required = true) final String issuerEmailAddress,
                          @JsonProperty(required = true) final Set<UUID> attachmentsIds) {
        this.title = title;
        this.message = message;
        this.issuerEmailAddress = issuerEmailAddress;
        this.attachmentsIds = attachmentsIds;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public String getIssuerEmailAddress() {
        return issuerEmailAddress;
    }

    public void setIssuerEmailAddress(final String issuerEmailAddress) {
        this.issuerEmailAddress = issuerEmailAddress;
    }

    public Set<UUID> getAttachmentsIds() {
        return attachmentsIds;
    }

    public void setAttachmentsIds(final Set<UUID> attachmentsIds) {
        this.attachmentsIds = attachmentsIds;
    }
}
