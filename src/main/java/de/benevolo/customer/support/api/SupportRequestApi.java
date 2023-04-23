package de.benevolo.customer.support.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/support/request")
public class SupportRequestApi {

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void requestSupport() {
    }

}
