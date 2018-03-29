package com.despe.despe.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Created by marcelo.cruz on 29/03/18.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RequestObject {

    private String id;
    private Root root;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Root getRoot() {
        return root;
    }

    public void setRoot(Root root) {
        this.root = root;
    }

    @Override
    public String toString(){
        return ReflectionToStringBuilder.toString(this, ToStringStyle.JSON_STYLE);
    }
}
