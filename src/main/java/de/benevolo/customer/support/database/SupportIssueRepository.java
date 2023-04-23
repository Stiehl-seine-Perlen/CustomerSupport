package de.benevolo.customer.support.database;

import de.benevolo.customer.support.entities.SupportIssue;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

public interface SupportIssueRepository extends PanacheRepository<SupportIssue> {
}
