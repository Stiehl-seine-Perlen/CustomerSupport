package de.benevolo.customer.support.entities.testdata;

import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.SupportIssueStatus;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TestSupportIssues {

    private static final String[] validTitles = {
            "Euer Produkt ist sch**** >:(",
            "So ein schlechtes Produkt!",
            "Ihr seid doch alle inkompetent!",
            "Ich kündige diesen Mist wenn das so weiter geht!",
            "Viel Spaß beim Übersetzen: 只有傻瓜才會讀這個"
    };

    private static final String[] invalidTitles = {
            null,
            "",
            "    "
    };

    private static final String[] validEmails = {
            "some@email.de",
            "another.very.long.email@server.de"
    };

    private static final String[] invalidEmails = {
            null,
            "",
            "   ",
            "thisisnotanemail",
            "<weidhackerthing/>@gmail.com"
    };

    /**
     * Generates a random valid {@link SupportIssue} for testing issues.
     *
     * @return valid entity that should pass validation checks
     * @author Daniel Mehlber
     */
    public static SupportIssue getRandomValid() {
        final String title = validTitles[new Random().nextInt(validTitles.length)];
        final String email = validEmails[new Random().nextInt(validEmails.length)];

        return new SupportIssue(title, email, SupportIssueStatus.OPEN);
    }

    /**
     * Generates a random valid {@link SupportIssue} for testing issues.
     *
     * @return valid entity that should pass validation checks
     * @author Daniel Mehlber
     */
    public static SupportIssue getRandomInvalid() {
        final String title = invalidTitles[new Random().nextInt(invalidTitles.length)];
        final String email = invalidEmails[new Random().nextInt(invalidEmails.length)];

        return new SupportIssue(title, email, SupportIssueStatus.OPEN);
    }

    /**
     * Returns all invalid entity combinations of {@link SupportIssue}. These issues should not pass validation checks.
     *
     * @return list of invalid {@link SupportIssue} entities
     * @author Daniel Mehlber
     */
    public static List<SupportIssue> getAllInvalidCombinations() {
        final List<SupportIssue> invalidIssues = new LinkedList<>();

        // all invalid titles
        for (final String invalidTitle : invalidTitles) {
            final SupportIssue issue = getRandomValid();
            issue.setTitle(invalidTitle);
            invalidIssues.add(issue);
        }

        // all invalid email addresses
        for (final String invalidEmails : invalidEmails) {
            final SupportIssue issue = getRandomValid();
            issue.setIssuerEmailAddress(invalidEmails);
            invalidIssues.add(issue);
        }

        return invalidIssues;
    }

    /**
     * Returns all valid entity combinations of {@link SupportIssue}. These issues should all pass validation checks.
     *
     * @return list of valid {@link SupportIssue} entities
     * @author Daniel Mehlber
     */
    public static List<SupportIssue> getAllValidCombinations() {
        final List<SupportIssue> validIssues = new LinkedList<>();

        for (final String validTitle : validTitles) {
            for (final String validEmail : validEmails) {
                final SupportIssue validIssue = new SupportIssue(validTitle, validEmail, SupportIssueStatus.OPEN);
                validIssues.add(validIssue);
            }
        }

        return validIssues;
    }

}
