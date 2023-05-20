package de.benevolo.customer.support.entities.testdata;

import de.benevolo.customer.support.entities.CustomerFeedback;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestCustomerFeedback {

    private static final String[] validMessages = {
            "Euer Service ist echt lausig, das nächste Mal versuche ich das Problem selbst zu lösen",
            "Der Support-Prozess ist ja meega! So einen guten habe ich noch nie gesehen!",
            "Viel Spaß beim übersetzen: 任何讀到這篇文章的人都是愚蠢的，需要一個新的支持團隊"
    };

    public static CustomerFeedback getRandomValid() {
        final String message = validMessages[new Random().nextInt(validMessages.length)];
        final short rating = (short) new Random().nextInt(1, 5);

        return new CustomerFeedback(rating, message);
    }

    public static List<CustomerFeedback> getAllValidCombinations() {
        final List<CustomerFeedback> feedbacks = new LinkedList<>();
        for (final String message : validMessages) {
            final short rating = (short) new Random().nextInt(1, 5);
            final CustomerFeedback feedback = new CustomerFeedback(rating, message);
            feedbacks.add(feedback);
        }

        return feedbacks;
    }

}
