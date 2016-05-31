package com.petterlysne.s198579;

// POJO klasse for wikipedia artikler
public class Page {

    private int pageid;
    private String imageURL;
    private String content;
    private String title;

    public Page() {
        pageid = 0;
        imageURL = "";
        content = "";
        title = "";
    }

    public Page(int pageid, String imageURL, String content, String title) {
        this.pageid = pageid;
        this.imageURL = imageURL;
        this.content = content;
        this.title = title;
    }

    public int getPageid() {
        return pageid;
    }

    public void setPageid(int pageid) {
        this.pageid = pageid;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
