package com.despe.despe.model;

import java.util.Date;

/**
 * Created by marcelo.cruz on 04/04/18.
 */
public class LineBack {

    private String id;
    private Date start;
    private Date end;
    private String service;
    private String span;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSpan() {
        return this.span;
    }

    public void setSpan(String span) {
        this.span = span;
    }
}
