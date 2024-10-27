package com.example.appdocsachfinal.Model;

public class ModelListPdf {
    String id, title, url;
    long viewsCount,downloadsCount;


    public ModelListPdf() {
    }

    public ModelListPdf(String id, String title, String url, long viewsCount, long downloadsCount) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.viewsCount = viewsCount;
        this.downloadsCount = downloadsCount;
    }

    public long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(long viewsCount) {
        this.viewsCount = viewsCount;
    }

    public long getDownloadsCount() {
        return downloadsCount;
    }

    public void setDownloadsCount(long downloadsCount) {
        this.downloadsCount = downloadsCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}

