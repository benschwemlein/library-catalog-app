package com.example.library.pattern.template;

import java.util.List;
import java.util.Map;

public class ReportSection {

    private final String heading;
    private final List<Map<String, Object>> data;
    private final String summary;

    public ReportSection(String heading, List<Map<String, Object>> data, String summary) {
        this.heading = heading;
        this.data = data;
        this.summary = summary;
    }

    public String getHeading() { return heading; }
    public List<Map<String, Object>> getData() { return data; }
    public String getSummary() { return summary; }
}
