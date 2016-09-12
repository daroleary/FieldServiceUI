package org.fieldservice.ui;

import org.fieldservice.ui.signals.daily.DailySignalResponse;
import org.fieldservice.ui.signals.daily.EquipmentResponse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import java.text.MessageFormat;
import java.util.List;

/**
 */
@ApplicationScoped
public class JsonService {

    private Client client;
    private WebTarget target;

    @PostConstruct
    protected void init() {
        client = ClientBuilder.newClient();
        target = client.target(
                "http://localhost:8180/fieldservice-1.0-SNAPSHOT/rest/");
    }

    protected WebTarget  getWebTarget() {
        return target;
    }

    public List<EquipmentResponse> getEquipmentResponses() {

        //noinspection EmptyClass
        return getWebTarget().path("equipments/simple")
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<EquipmentResponse>>(){});
    }

    public List<DailySignalResponse> getDailySignalResponses(Long equipmentId) {
        String path = MessageFormat.format("signals/daily/equipment/{0}", equipmentId);
        return getSignals(path);
    }

    public List<DailySignalResponse> getSignals(String path) {
        //TODO: make it generic

        //noinspection EmptyClass
        return getWebTarget().path(path)
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<DailySignalResponse>>(){});
    }
}
