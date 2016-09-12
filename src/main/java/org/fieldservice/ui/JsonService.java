package org.fieldservice.ui;

import org.fieldservice.ui.response.signals.DailySignalResponse;
import org.fieldservice.ui.response.signals.EquipmentResponse;
import org.fieldservice.ui.response.signals.MonthlySignalResponse;
import org.fieldservice.ui.response.signals.YearlySignalResponse;

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

        //noinspection EmptyClass
        return getWebTarget().path("signals/daily/equipments" + getPostFix(equipmentId))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<DailySignalResponse>>(){});
    }

    public List<MonthlySignalResponse> getMonthlySignalResponses(Long equipmentId) {
        //noinspection EmptyClass
        return getWebTarget().path("signals/monthly/equipments" + getPostFix(equipmentId))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<MonthlySignalResponse>>(){});
    }

    public List<YearlySignalResponse> getYearlySignalResponses(Long equipmentId) {
        //noinspection EmptyClass
        return getWebTarget().path("signals/yearly/equipments" + getPostFix(equipmentId))
                .request(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<YearlySignalResponse>>(){});
    }

    private String getPostFix(Long equipmentId) {
        return equipmentId == null ? "" : MessageFormat.format("/{0}", equipmentId);
    }
}
