package de.benevolo.customer.support.database;

import de.benevolo.customer.support.entities.SupportIssueMessage;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SupportIssueMessageRepository implements PanacheRepository<SupportIssueMessage> {
}
