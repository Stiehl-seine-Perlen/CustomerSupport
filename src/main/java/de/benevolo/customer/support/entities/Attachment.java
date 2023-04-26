package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Attachment {

    @Id
    private UUID id;

    @NotBlank
    private String filename;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "messageId", nullable = false)
    private SupportIssueMessage message;

    @JsonCreator
    public Attachment(@JsonProperty final UUID id,
                      @JsonProperty final String filename) {
        this.filename = filename;
        this.id = id;
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Attachment that = (Attachment) o;
        return Objects.equals(id, that.id) && Objects.equals(filename, that.filename) && Objects.equals(message.getId(), that.message.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, filename);
    }

    @Override
    public String toString() {
        return "Attachment{" +
                "id=" + id +
                ", filename='" + filename + '\'' +
                '}';
    }
}
