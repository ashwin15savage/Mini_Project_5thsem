package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ContactList implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("subject")
    private String subject;

    public ContactList(String id, String subject) {
        this.id = id;
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public String getSubject() {
        return subject;
    }
}
