package de.benevolo.customer.support.database;

import de.benevolo.customer.support.entities.Attachment;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class AttachmentRepository implements PanacheRepositoryBase<Attachment, UUID> {
}
