package de.benevolo.customer.support.entities;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * This contains feedback given by the customer at the end of an issue resolving process.
 *
 * @author Daniel Mehlber
 */
@Entity
public class CustomerFeedback {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * the star rating from 1 to 5 stars. The higher the amount of stars, the better the service.
     */
    @Min(value = 1, message = "1 stars is the lowest rating")
    @Max(value = 5, message = "5 stars is the highest rating")
    private short ratingStars;

    private String message;

    @JsonIgnore
    @OneToOne
    @JoinColumn
    private SupportIssue issue;

    private LocalDateTime creation;

    @PrePersist
    protected void persist() {
        creation = LocalDateTime.now();
    }

    protected CustomerFeedback() {
    }

    @JsonCreator
    public CustomerFeedback(@JsonProperty(required = true) final short ratingStars,
                            @JsonProperty(required = false) final String message) {
        this.message = message;
        this.ratingStars = ratingStars;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public short getRatingStars() {
        return ratingStars;
    }

    public void setRatingStars(final short ratingStars) {
        this.ratingStars = ratingStars;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public SupportIssue getIssue() {
        return issue;
    }

    public void setIssue(final SupportIssue issue) {
        this.issue = issue;
        if (issue != null && issue.getFeedback() != this) {
            issue.setFeedback(this);
        }
    }

    public LocalDateTime getCreation() {
        return creation;
    }

    public void setCreation(final LocalDateTime creation) {
        this.creation = creation;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final CustomerFeedback that = (CustomerFeedback) o;
        return ratingStars == that.ratingStars && Objects.equals(id, that.id) && Objects.equals(message, that.message) && Objects.equals(creation, that.creation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, ratingStars, message, creation);
    }

    @Override
    public String toString() {
        return "CustomerFeedback{" +
                "id=" + id +
                ", ratingStars=" + ratingStars +
                ", message='" + message + '\'' +
                ", creation=" + creation +
                '}';
    }
}
