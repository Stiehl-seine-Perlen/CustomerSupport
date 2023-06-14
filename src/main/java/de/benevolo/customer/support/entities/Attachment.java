package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;


/**
 * An attachment that can be used by other entities and references a file stored on the s3 bucket.
 */
@Entity
public class Attachment {

    /**
     * unique id of this attachment that will be referenced by other entities using it.
     */
    @Id
    private UUID id;


    private String location;

    /**
     * filename of the attachment for display and metadata purposes
     */
    @NotBlank(message = "filename of attachment must not be blank")
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    @JsonIgnore
    private SupportIssueMessage message;

    @JsonCreator
    public Attachment(@JsonProperty final UUID id,
                      @JsonProperty final String filename,
                      @JsonProperty final String location) {
        this.filename = filename;
        this.id = id;
        this.location = location;
    }

    protected Attachment() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    public SupportIssueMessage getMessage() {
        return message;
    }

    public void setMessage(final SupportIssueMessage message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(final String location) {
        this.location = location;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Attachment that = (Attachment) o;
        return Objects.equals(id, that.id) && Objects.equals(location, that.location) && Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, filename);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", location='" + location + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}
