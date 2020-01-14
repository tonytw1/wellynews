package nz.co.searchwellington.model;

import java.io.Serializable;

public class Image implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String url;
    private String description;

    public Image() {
    }

    public Image(String url, String description) {
        this.url = url;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}