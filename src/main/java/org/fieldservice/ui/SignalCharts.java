package org.fieldservice.ui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.AxisType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.DataSeries;
import com.vaadin.addon.charts.model.DataSeriesItem;
import com.vaadin.addon.charts.model.Labels;
import com.vaadin.addon.charts.model.PlotLine;
import com.vaadin.addon.charts.model.PlotOptionsSeries;
import com.vaadin.addon.charts.model.RangeSelector;
import com.vaadin.addon.charts.model.Tooltip;
import com.vaadin.addon.charts.model.XAxis;
import com.vaadin.addon.charts.model.YAxis;
import com.vaadin.addon.charts.model.style.SolidColor;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.text.WordUtils;
import org.fieldservice.ui.signals.daily.DailyCount;
import org.fieldservice.ui.signals.daily.DailySignalData;
import org.fieldservice.ui.signals.daily.EquipmentResponse;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@CDIUI("")
@Theme("mytheme")
@Widgetset("org.fieldservice.ui.AppWidgetSet")
public class SignalCharts extends UI {
    @Inject
    JsonService service;

    private static final ImmutableMap<EquipmentStatusCode, SolidColor> STATUS_TO_COLOR
            = new ImmutableMap.Builder<EquipmentStatusCode, SolidColor>()
            .put(EquipmentStatusCode.ACTIVE, SolidColor.GREEN)
            .put(EquipmentStatusCode.ENGAGED, SolidColor.BLUE)
            .put(EquipmentStatusCode.LOAD, SolidColor.RED)
            .put(EquipmentStatusCode.OVERRIDE, SolidColor.LIGHTGREEN)
            .put(EquipmentStatusCode.UNPLUG, SolidColor.BLACK)
            .build();

    @Override
    protected void init(VaadinRequest request) {

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        final BeanItemContainer<EquipmentResponse> container =
                new BeanItemContainer<>(EquipmentResponse.class);

        List<EquipmentResponse> equipmentResponses = service.getEquipmentResponses();
        for (EquipmentResponse equipmentResponse : equipmentResponses) {
            String assetNumber = equipmentResponse.getAssetNumber();
            Long equipmentId = equipmentResponse.getEquipmentId();

            container.addBean(new EquipmentResponse(equipmentId, assetNumber));
        }

        ComboBox comboBox = new ComboBox("Select the Asset", container);

        comboBox.setInputPrompt("No asset selected");
        comboBox.setPageLength(5);

        // Set the appropriate filtering mode for this example
        comboBox.setFilteringMode(FilteringMode.CONTAINS);
        comboBox.setImmediate(true);

        comboBox.setNullSelectionAllowed(false);
        comboBox.setItemCaptionPropertyId("name");

        comboBox.addValueChangeListener(e -> Notification.show("Value changed:",
                                                               String.valueOf(e.getProperty().getValue()),
                                                               Notification.Type.TRAY_NOTIFICATION));


        DailySignalData dailySignalData = new DailySignalData(service.getDailySignalResponses(1L));
        layout.addComponent(getDailyChart(dailySignalData));
        layout.addComponent(comboBox);
        layout.setComponentAlignment(comboBox, Alignment.BOTTOM_CENTER);
    }

    private Chart getDailyChart(DailySignalData dailySignalData) {
        Chart chart = new Chart();
        Configuration conf = initialConfiguration(chart);

        ImmutableMultimap<EquipmentStatusCode, DailyCount> dailyCountByEquipmentStatus = dailySignalData.getDailyCountByEquipmentStatus();

        long weeksBetween = getWeeksBetween(dailyCountByEquipmentStatus);

        for (EquipmentStatusCode statusCode : dailyCountByEquipmentStatus.keySet()) {
            ImmutableCollection<DailyCount> dailyCounts = dailyCountByEquipmentStatus.get(statusCode);

            DataSeries dailyCountData = new DataSeries(WordUtils.capitalizeFully(statusCode.getName()));
            for (DailyCount dailyCount : dailyCounts) {
                dailyCountData.add(new DataSeriesItem(
                        dailyCount.getEntryDate(),
                        dailyCount.getCount()
                ));
            }

            PlotOptionsSeries plotOpts = new PlotOptionsSeries();
            plotOpts.setColor(STATUS_TO_COLOR.get(statusCode));
            dailyCountData.setPlotOptions(plotOpts);

            conf.addSeries(dailyCountData);
        }

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(weeksBetween);
        conf.setRangeSelector(rangeSelector);

        chart.drawChart(conf);

        return chart;
    }

    private Configuration initialConfiguration(Chart chart) {
        Configuration conf = chart.getConfiguration();
        conf.setTitle("Daily Signals");

        chart.setHeight("450px");
        chart.setWidth("100%");
        chart.setTimeline(true);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Status (count)");
        Labels yLabel = new Labels();
        yAxis.setLabels(yLabel);
        yAxis.setType(AxisType.LOGARITHMIC);

        XAxis xAxis = new XAxis();
        xAxis.setTitle("Date");
        Labels xLabel = new Labels();
        xAxis.setLabels(xLabel);
        xAxis.setType(AxisType.DATETIME);

        PlotLine plotLine = new PlotLine();
        plotLine.setValue(2);
        plotLine.setWidth(2);
        plotLine.setColor(SolidColor.SILVER);
        yAxis.setPlotLines(plotLine);

        conf.addyAxis(yAxis);
        conf.addxAxis(xAxis);

        Tooltip tooltip = new Tooltip();
        tooltip.setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}</b><br/>");
        tooltip.setValueDecimals(2);
        conf.setTooltip(tooltip);

        return conf;
    }

    private long getWeeksBetween(ImmutableMultimap<EquipmentStatusCode, DailyCount> dailyCountByEquipmentStatus) {
        Optional<LocalDate> minDate = dailyCountByEquipmentStatus.values().stream()
                .map(DailyCount::getEntryDate)
                .min(Date::compareTo)
                .map(date -> {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("MST7MDT"));
                    return zonedDateTime.toLocalDate();
                });

        if (!minDate.isPresent()) {
            return 0;
        }

        //noinspection OptionalGetWithoutIsPresent
        LocalDate maxDate = dailyCountByEquipmentStatus.values().stream()
                .map(DailyCount::getEntryDate)
                .min(Date::compareTo)
                .map(date -> {
                    ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("MST7MDT"));
                    return zonedDateTime.toLocalDate();
                })
                .get();

        long weeksBetween = ChronoUnit.WEEKS.between(maxDate, maxDate);
        return weeksBetween == 0 ? 1 : weeksBetween;
    }
}
