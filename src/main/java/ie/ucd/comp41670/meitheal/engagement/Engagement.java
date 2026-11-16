package ie.ucd.comp41670.meitheal.engagement;

import java.util.Objects;

public class Engagement {

    private final String id;
    private final Request request;
    private final Offer offer;

    private boolean requesterConfirmed;
    private boolean providerConfirmed;

    public Engagement(
            String id,
            Request request,
            Offer offer) {

        this.id = Objects.requireNonNull(id);
        this.request = Objects.requireNonNull(request);
        this.offer = Objects.requireNonNull(offer);

        this.requesterConfirmed = false;
        this.providerConfirmed = false;
    }

    public String getId() {
        return id;
    }

    public Request getRequest() {
        return request;
    }

    public Offer getOffer() {
        return offer;
    }

    public boolean isRequesterConfirmed() {
        return requesterConfirmed;
    }

    public boolean isProviderConfirmed() {
        return providerConfirmed;
    }

    public void confirmByRequester() {
        requesterConfirmed = true;
    }

    public void confirmByProvider() {
        providerConfirmed = true;
    }

    public boolean isConfirmed() {
        return requesterConfirmed && providerConfirmed;
    }
}
