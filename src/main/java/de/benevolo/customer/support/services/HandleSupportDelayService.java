package de.benevolo.customer.support.services;

import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class HandleSupportDelayService {

    @Inject
    Logger log;

    public String generateCustomerApology(final Long issueId) {
        log.infof("generated apology for customer due to support delay for issue id:%d", issueId);
        return String.format("sowweee, resolving your issue %d: we are lazy and slooow", issueId);
    }

    public String generateSupportTeamNotification(final Long issueId) {
        log.infof("generated support team notification due to support delay for issue id:%d", issueId);
        return String.format("Get your shit together! Issue %d is still unresolved!", issueId);
    }

}
