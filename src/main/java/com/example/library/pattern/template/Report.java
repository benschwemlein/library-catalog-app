package com.example.library.pattern.template;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class Report {

    private String title;
    private LocalDateTime generatedAt;
    private Object criteria;
    private List<ReportSection> sections;
    private Map<String, Object> metadata;

    public Report(String title, LocalDateTime generatedAt, Object criteria,
                  List<ReportSection> sections, Map<String, Object> metadata) {
        this.title = title;
        this.generatedAt = generatedAt;
        this.criteria = criteria;
        this.sections = sections;
        this.metadata = metadata;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public LocalDateTime getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(LocalDateTime generatedAt) { this.generatedAt = generatedAt; }
    public Object getCriteria() { return criteria; }
    public void setCriteria(Object criteria) { this.criteria = criteria; }
    public List<ReportSection> getSections() { return sections; }
    public void setSections(List<ReportSection> sections) { this.sections = sections; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
