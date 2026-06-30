package com.example.library.pattern.template;

import java.util.List;
import java.util.Map;

public class ReportData {

    private Map<String, Object> rawData;
    private List<Map<String, Object>> rows;
    private Map<String, Object> aggregates;

    public ReportData(Map<String, Object> rawData, List<Map<String, Object>> rows, Map<String, Object> aggregates) {
        this.rawData = rawData;
        this.rows = rows;
        this.aggregates = aggregates;
    }

    public Map<String, Object> getRawData() { return rawData; }
    public void setRawData(Map<String, Object> rawData) { this.rawData = rawData; }
    public List<Map<String, Object>> getRows() { return rows; }
    public void setRows(List<Map<String, Object>> rows) { this.rows = rows; }
    public Map<String, Object> getAggregates() { return aggregates; }
    public void setAggregates(Map<String, Object> aggregates) { this.aggregates = aggregates; }
}
