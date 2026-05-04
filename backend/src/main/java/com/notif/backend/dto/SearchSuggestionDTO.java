package com.notif.backend.dto;

public class SearchSuggestionDTO {

    private String id;
    private String title;
    private String venue;
    private String city;
    private String date;
    private String time;

    public SearchSuggestionDTO() {
    }

    public SearchSuggestionDTO(String id, String title, String venue, String city, String date, String time) {
        this.id = id;
        this.title = title;
        this.venue = venue;
        this.city = city;
        this.date = date;
        this.time = time;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getVenue() {
        return venue;
    }

    public String getCity() {
        return city;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }
}