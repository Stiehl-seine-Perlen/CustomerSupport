package de.benevolo.customer.support.api;

import de.benevolo.customer.support.database.SupportIssueRepository;
import de.benevolo.customer.support.entities.SupportIssue;
import de.benevolo.customer.support.entities.testdata.TestSupportIssues;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SupportIssueResourceTest {

    @Inject
    SupportIssueRepository issueRepository;

    @Inject
    EntityManager entityManager;

    @BeforeEach
    @Transactional
    public void prepare() {
        issueRepository.deleteAll();
    }

    @AfterEach
    @Transactional
    public void cleanUp() {
        issueRepository.deleteAll();
    }

    @Test
    @DisplayName("should fetch issue by id")
    public void fetchSupportIssue() throws IOException {
        // -- ARRANGE --
        final SupportIssue issue = TestSupportIssues.getRandomValid();

        persistIssue(issue);

        // -- ACT --
        final String json = given().pathParam("id", issue.getId())
                .when().get("/issue/{id}")
                .then().statusCode(200).and().extract().body().asString();

        final ObjectMapper mapper = new ObjectMapper();
        final SupportIssue fetched = mapper.readValue(json, SupportIssue.class);

        // -- ASSERT --
        assertEquals(issue, fetched);
    }


    @Transactional
    public void persistIssue(final SupportIssue issue) {
        issueRepository.persist(issue);
    }

}
