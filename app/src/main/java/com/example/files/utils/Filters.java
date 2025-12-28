package com.example.files.utils;

import com.example.files.models.JFile;

public class Filters {

    String text;
    long date;
    JFile.Type type;

    public Filters() {
        initFilters();
    }

    public void initFilters() {
        this.text = "";
        this.date = 0;
        this.type = null;
    }

    public Filters setTextFilter(String text) {
        this.text = text.trim();
        return this;
    }

    public Filters setDateFilter(long date) {
        this.date = date;
        return this;
    }

    public Filters setTypeFilter(JFile.Type type) {
        this.type = type;
        return this;
    }

    public String getTextFilter() {
        return text.toLowerCase();
    }

    public long getDateFilter() {
        return date;
    }

    public JFile.Type getTypeFilter() {
        return type;
    }

    public void clearFilters() {
        initFilters();
    }

    public void clearDateFilter() {
        this.date = 0;
    }

    public void clearTypeFilter() {
        this.type = null;
    }

    //    List<Filter> filters;
//    private final int TEXT_FILTER = 0, DATE_FILTER = 1;
//
//    public Filters() {
//        filters = new ArrayList<>();
//        initFilters();
//    }
//
//    private void initFilters() {
//        filters.add(new TextFilter(""));
//        filters.add(new DateFilter(0));
//    }
//
//    public List<Filter> getFilters() {
//        return filters;
//    }
//
//    public String getTextFilter() {
//        if (filters.get(TEXT_FILTER) != null && filters.get(TEXT_FILTER).getFilterType() == Filter.FilterType.TextFilter)
//        return filters.get(TEXT_FILTER).getText();
//        else return "";
//    }
//
//    public long getDateFilters() {
//        if (filters.get(DATE_FILTER) != null && filters.get(DATE_FILTER).getFilterType() == Filter.FilterType.DateFilter)
//            return filters.get(DATE_FILTER).getDate();
//        else return 0;
//    }
//
//    public void addFilter(Filter filter) {
//        filters.add(filter);
//    }
//
//    public void removeFilter(Filter filter) {
//        filters.remove(filter);
//    }
//
//    public void clearFilters() {
//        filters.clear();
//        initFilters();
//    }
//
//    public void clearDateFilters() {
//        filters.removeIf(filter -> filter.getFilterType() == Filter.FilterType.DateFilter);
//    }
//
//    public static class Filter {
//        String text;
//        long Date;
//        FilterType filterType;
//        public enum FilterType {DateFilter, TextFilter};
//
//        Filter(FilterType filterType) {
//            this.filterType = filterType;
//        }
//
//        public FilterType getFilterType() {
//            return filterType;
//        }
//
//        public String getText() {
//            return text;
//        }
//
//        public long getDate() {
//            return Date;
//        }
//    }
//    public static class TextFilter extends Filter {
//        String text;
//        TextFilter(String text) {
//            super(FilterType.TextFilter);
//            this.text = text;
//        }
//    }
//    public static class DateFilter extends Filter{
//        long date;
//        DateFilter(long date) {
//            super(FilterType.DateFilter);
//            this.date = date;
//        }
//    }
}
