package de.benevolo.customer.support.services;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class HandleSupportDelayService {

    public String generateCustomerApology(final Long issueId) {
        return String.format("sowweee, resolving your issue %d: we are lazy and slooow", issueId);
    }

    public String generateSupportTeamNotification(final Long issueId) {
        return String.format("Get your shit together! Issue %d is still unresolved!", issueId);
    }

}
