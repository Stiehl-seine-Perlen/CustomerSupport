package de.benevolo.customer.support.entities;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

@Entity
public class Attachment {

    @Id
    private UUID id;

    @NotNull
    private byte[] content;

    @NotBlank
    private String filename;
    
    @ManyToOne
    @JoinColumn(name = "messageId", nullable = false)
    private SupportIssueMessage message;

    @JsonCreator
    public Attachment(@JsonProperty final UUID id,
                      @JsonProperty final String filename,
                      @JsonProperty final byte[] content) {
        this.content = content;
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

    public byte[] getContent() {
        return content;
    }

    public void setContent(final byte[] content) {
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(final String filename) {
        this.filename = filename;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Attachment that = (Attachment) o;
        return Objects.equals(id, that.id) && Arrays.equals(content, that.content) && Objects.equals(filename, that.filename);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, filename);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
}
