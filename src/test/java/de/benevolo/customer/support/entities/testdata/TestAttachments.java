package de.benevolo.customer.support.entities.testdata;

import de.benevolo.customer.support.entities.Attachment;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestAttachments {

    public static final String[] validFilenames = {
            "filename.png",
            "verylongfilenamethatistotallyunnececcary.filenamextension"
    };

    public static final String[] invalidFilenames = {
            null,
            "",
            "   "
    };

    public static Attachment getRandomValid() {
        final String filename = validFilenames[new Random().nextInt(validFilenames.length)];
        return new Attachment(UUID.randomUUID(), filename);
    }

    public static List<Attachment> getAllValidCombinations() {
        final List<Attachment> attachments = new LinkedList<>();

        for (final String validFilename : validFilenames) {
            final Attachment attachment = new Attachment(UUID.randomUUID(), validFilename);
            attachments.add(attachment);
        }

        return attachments;
    }

    public static List<Attachment> getAllInvalidCombinations() {
        final List<Attachment> attachments = new LinkedList<>();

        for (final String invalidFilename : invalidFilenames) {
            final Attachment attachment = new Attachment(UUID.randomUUID(), invalidFilename);
            attachments.add(attachment);
        }

        return attachments;
    }

}
