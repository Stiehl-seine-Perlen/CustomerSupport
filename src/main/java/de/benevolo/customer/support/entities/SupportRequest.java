package de.benevolo.customer.support.entities;

import javax.validation.constraints.NotBlank;

/**
 * This is sent when a user wants to open a new support issue
 */
public class SupportRequest {

    @NotBlank(message = "title must not be empty")
    private String title;

    @NotBlank(message = "summary must not be empty")
    private String summary;

    @NotBlank(message = "the issuers email address may not be blank")
    private String issuerEmailAddress;

    protected SupportRequest() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(final String summary) {
        this.summary = summary;
    }

    public String getIssuerEmailAddress() {
        return issuerEmailAddress;
    }

    public void setIssuerEmailAddress(final String issuerEmailAddress) {
        this.issuerEmailAddress = issuerEmailAddress;
    }
}
