package de.benevolo.customer.support.database;

import de.benevolo.customer.support.entities.Attachment;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

public interface AttachmentRepository extends PanacheRepository<Attachment> {
}
