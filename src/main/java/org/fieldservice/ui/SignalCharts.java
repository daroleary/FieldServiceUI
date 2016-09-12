package org.fieldservice.ui;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableSet;
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
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.apache.commons.lang3.text.WordUtils;
import org.fieldservice.ui.response.signals.DailyCount;
import org.fieldservice.ui.response.signals.DailySignalResponse;
import org.fieldservice.ui.response.signals.EquipmentResponse;
import org.fieldservice.ui.response.signals.MonthlySignalResponse;
import org.fieldservice.ui.response.signals.SignalData;
import org.fieldservice.ui.response.signals.SignalResponse;
import org.fieldservice.ui.response.signals.YearlySignalResponse;

import javax.inject.Inject;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CDIUI("")
@Theme("mytheme")
@Widgetset("org.fieldservice.ui.AppWidgetSet")
public class SignalCharts extends UI {
    @Inject
    JsonService service;

    private final Map<EquipmentStatusCode, DataSeries> STATUS_TO_DATA_SERIES;

    private static final ImmutableMap<Period, String> PERIOD_TO_TITLE
            = new ImmutableMap.Builder<Period, String>()
            .put(Period.DAILY, "Daily Signals")
            .put(Period.MONTHLY, "Monthly Signals")
            .put(Period.YEARLY, "Yearly Signals")
            .build();

    private static final ImmutableMap<Period, String> PERIOD_TO_XLABEL
            = new ImmutableMap.Builder<Period, String>()
            .put(Period.DAILY, "Daily")
            .put(Period.MONTHLY, "Month")
            .put(Period.YEARLY, "Year")
            .build();

    private static final ImmutableMap<EquipmentStatusCode, SolidColor> STATUS_TO_COLOR
            = new ImmutableMap.Builder<EquipmentStatusCode, SolidColor>()
            .put(EquipmentStatusCode.ACTIVE, SolidColor.GREEN)
            .put(EquipmentStatusCode.ENGAGED, SolidColor.BLUE)
            .put(EquipmentStatusCode.LOAD, SolidColor.RED)
            .put(EquipmentStatusCode.OVERRIDE, SolidColor.LIGHTGREEN)
            .put(EquipmentStatusCode.UNPLUG, SolidColor.BLACK)
            .build();

    private Chart _chart;
    private Configuration _conf;
    private DataSeries _series;
    private Period _period;
    private Long _equipmentId = null;

    public SignalCharts() {

        STATUS_TO_DATA_SERIES = new HashMap<>();
        STATUS_TO_DATA_SERIES.put(EquipmentStatusCode.ACTIVE,
                                  new DataSeries(WordUtils.capitalizeFully(EquipmentStatusCode.ACTIVE.getName())));
        STATUS_TO_DATA_SERIES.put(EquipmentStatusCode.ENGAGED,
                                  new DataSeries(WordUtils.capitalizeFully(EquipmentStatusCode.ACTIVE.getName())));
        STATUS_TO_DATA_SERIES.put(EquipmentStatusCode.LOAD,
                                  new DataSeries(WordUtils.capitalizeFully(EquipmentStatusCode.ACTIVE.getName())));
        STATUS_TO_DATA_SERIES.put(EquipmentStatusCode.OVERRIDE,
                                  new DataSeries(WordUtils.capitalizeFully(EquipmentStatusCode.ACTIVE.getName())));
        STATUS_TO_DATA_SERIES.put(EquipmentStatusCode.UNPLUG,
                                  new DataSeries(WordUtils.capitalizeFully(EquipmentStatusCode.ACTIVE.getName())));
    }

    @Override
    protected void init(VaadinRequest request) {

        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        ComboBox comboBox = getComboBox();
        layout.addComponent(comboBox);
        layout.setComponentAlignment(comboBox, Alignment.TOP_LEFT);

        SignalData signalData = new SignalData(service.getMonthlySignalResponses(null));
        layout.addComponent(getDailyChart(signalData));

        NativeSelect nativeSelect = getPeriodSelect();
        layout.addComponent(nativeSelect);
        layout.setComponentAlignment(nativeSelect, Alignment.BOTTOM_CENTER);
    }

    private NativeSelect getPeriodSelect() {
        NativeSelect nativeSelect = new NativeSelect();

        for (Period period : ImmutableSet.of(Period.DAILY, Period.YEARLY)) {

            String periodName = getFormattedPeriodName(period);

            nativeSelect.addItem(periodName);
        }
        _period = Period.MONTHLY;
        nativeSelect.setNullSelectionItemId(getFormattedPeriodName(Period.MONTHLY));
        nativeSelect.addValueChangeListener((Property.ValueChangeListener) event -> {

            //TODO: occationally returning as null causing issues. Need to investigate further.
            String periodStr = (String) event.getProperty().getValue();

            Period period = getPeriodFrom(periodStr);
            _period = period;
            SignalData signalData = new SignalData(getSignalResponse(period));
            updateChart(signalData, false);
            _chart.drawChart();
        });
        nativeSelect.setImmediate(true);

        return nativeSelect;
    }

    private String getFormattedPeriodName(Period period) {
        return WordUtils.capitalizeFully(period.name());
    }

    private Period getPeriodFrom(String period) {
        return Period.valueOf(period.toUpperCase());
    }

    private ComboBox getComboBox() {
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

        comboBox.setNullSelectionAllowed(true);
        comboBox.setItemCaptionPropertyId("assetNumber");

        comboBox.addValueChangeListener(this::updateChartOnAssetChange);

        return comboBox;
    }

    private Chart getDailyChart(SignalData signalData) {
        _chart = new Chart();
        _conf = initialConfiguration(_chart, Period.MONTHLY);

        updateChart(signalData, true);
        return _chart;
    }

    private Configuration initialConfiguration(Chart chart, Period period) {
        Configuration conf = chart.getConfiguration();
        conf.setTitle(PERIOD_TO_TITLE.get(period));

        chart.setHeight("450px");
        chart.setWidth("100%");
        chart.setTimeline(true);

        YAxis yAxis = new YAxis();
        yAxis.setTitle("Status (count)");
        Labels yLabel = new Labels();
        yAxis.setLabels(yLabel);
        yAxis.setType(AxisType.LOGARITHMIC);

        XAxis xAxis = new XAxis();
        xAxis.setTitle(PERIOD_TO_XLABEL.get(period));
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
                .min(LocalDate::compareTo);

        if (!minDate.isPresent()) {
            return 0;
        }

        //noinspection OptionalGetWithoutIsPresent
        LocalDate maxDate = dailyCountByEquipmentStatus.values().stream()
                .map(DailyCount::getEntryDate)
                .min(LocalDate::compareTo)
                .get();

        long weeksBetween = ChronoUnit.WEEKS.between(maxDate, maxDate);
        return weeksBetween == 0 ? 1 : weeksBetween;
    }

    private void updateChartOnAssetChange(Property.ValueChangeEvent e) {

        EquipmentResponse equipmentResponse = (EquipmentResponse) e.getProperty().getValue();
        _equipmentId = equipmentResponse == null ? null : equipmentResponse.getEquipmentId();
        SignalData signalData = new SignalData(getSignalResponse(_period));

        updateChart(signalData, false);

        _chart.drawChart();
    }

    private void updateChart(SignalData signalData, boolean isCreate) {

        Period period = _period;

        if (!isCreate) {
            _conf.setTitle(PERIOD_TO_TITLE.get(period));
            _conf.getxAxis().setTitle(PERIOD_TO_XLABEL.get(period));
        }

        ImmutableMultimap<EquipmentStatusCode, DailyCount> dailyCountByEquipmentStatus = signalData.getDailyCountByEquipmentStatus();

        long weeksBetween = getWeeksBetween(dailyCountByEquipmentStatus);

        for (EquipmentStatusCode statusCode : dailyCountByEquipmentStatus.keySet()) {
            ImmutableCollection<DailyCount> dailyCounts = dailyCountByEquipmentStatus.get(statusCode);

            DataSeries dataSeries = STATUS_TO_DATA_SERIES.get(statusCode);
            dataSeries.clear();

            for (DailyCount dailyCount : dailyCounts) {
                Date date = Date.from(dailyCount.getEntryDate()
                                              .atStartOfDay()
                                              .atZone(ZoneId.systemDefault())
                                              .toInstant());
                dataSeries.add(new DataSeriesItem(
                        date,
                        dailyCount.getCount()
                ));
            }

            PlotOptionsSeries plotOpts = new PlotOptionsSeries();
            plotOpts.setColor(STATUS_TO_COLOR.get(statusCode));
            dataSeries.setPlotOptions(plotOpts);

            if (isCreate) {
                _conf.addSeries(dataSeries);
            }
        }

        RangeSelector rangeSelector = new RangeSelector();
        rangeSelector.setSelected(weeksBetween);
        _conf.setRangeSelector(rangeSelector);
    }

    @SuppressWarnings("unchecked")
    private <T extends SignalResponse> List<T> getSignalResponse(Period period) {

        List<T> response;

        switch (period) {
            case DAILY:
                response = (List<T>) getDailySignalResponses(_equipmentId);
                break;
            case MONTHLY:
                response = (List<T>) getMonthlySignalResponses(_equipmentId);
                break;
            case YEARLY:
                response = (List<T>) getYearlySignalResponses(_equipmentId);
                break;
            default:
                throw new RuntimeException(MessageFormat.format("Unexpected period of {0}", period));
        }

        return response;
    }

    private List<DailySignalResponse> getDailySignalResponses(Long equipmentId) {
        return service.getDailySignalResponses(equipmentId);
    }

    private List<MonthlySignalResponse> getMonthlySignalResponses(Long equipmentId) {
        return service.getMonthlySignalResponses(equipmentId);
    }

    private List<YearlySignalResponse> getYearlySignalResponses(Long equipmentId) {
        return service.getYearlySignalResponses(equipmentId);
    }
}
