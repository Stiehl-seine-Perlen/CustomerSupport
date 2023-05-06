package de.benevolo.customer.support.entities.testdata;

import de.benevolo.customer.support.entities.SupportIssueMessage;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestSupportIssueMessages {

    private static final String[] validMessages = {
            "So ein blödes Produkt, ich zünde euch alle an. Geht euch doch vergraben!",
            "Ich bin sooo sauer \n dass ich mehrere Zeilen \n brauche \n\n\n >:(",
            "Meine\tLeertaste\tist\tkaputt,\tdeshalb\tverwende\tich\ttab",
            "Ich verstehe, bitte versuchen Sie mal <script>alert('juckt mich nicht')</script> in Ihre Konsole ein.",
            "Ich mag euch nicht, deshalb viel Spaß beim Übersetzen: 如此愚蠢的產品，我希望你們都去死，一個也不留下"
    };

    private static final String[] invalidMessages = {
            null,
            "",
            "   "
    };

    public static SupportIssueMessage getRandomValid() {
        final String message = validMessages[new Random().nextInt(validMessages.length)];
        final boolean isFromCustomer = new Random().nextBoolean();
        return new SupportIssueMessage(message, isFromCustomer, new HashSet<>(), false);
    }

    public static SupportIssueMessage getRandomInvalid() {
        final String message = invalidMessages[new Random().nextInt(invalidMessages.length)];
        final boolean isFromCustomer = new Random().nextBoolean();
        return new SupportIssueMessage(message, isFromCustomer, new HashSet<>(), false);
    }

    public static List<SupportIssueMessage> getAllValidCombinations() {
        final List<SupportIssueMessage> messages = new LinkedList<>();

        for (final String validMessageContent : validMessages) {
            final SupportIssueMessage validMessage
                    = new SupportIssueMessage(validMessageContent, new Random().nextBoolean(), new HashSet<>(), false);
            messages.add(validMessage);
        }

        return messages;
    }

    public static List<SupportIssueMessage> getAllInvalidCombinations() {
        final List<SupportIssueMessage> messages = new LinkedList<>();

        for (final String invalidMessageContent : invalidMessages) {
            final SupportIssueMessage invalidMessage
                    = new SupportIssueMessage(invalidMessageContent, new Random().nextBoolean(), new HashSet<>(), false);
            messages.add(invalidMessage);
        }

        return messages;
    }

}
