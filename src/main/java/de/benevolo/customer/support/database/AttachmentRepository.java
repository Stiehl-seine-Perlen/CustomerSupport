package de.benevolo.customer.support.database;

import de.benevolo.customer.support.entities.Attachment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AttachmentRepository implements PanacheRepository<Attachment> {
}
