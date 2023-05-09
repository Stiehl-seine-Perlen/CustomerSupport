package de.benevolo.customer.support.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HandleSupportDelayService {

    private static final Logger LOG = LoggerFactory.getLogger(HandleSupportDelayService.class);

    public String generateCustomerApology(final Long issueId) {
        LOG.info("generated apology for customer due to support delay for issue id:{}", issueId);
        return String.format("sowweee, resolving your issue %d: we are lazy and slooow", issueId);
    }

    public String generateSupportTeamNotification(final Long issueId) {
        LOG.info("generated support team notification due to support delay for issue id:{}", issueId);
        return String.format("Get your shit together! Issue %d is still unresolved!", issueId);
    }

}
