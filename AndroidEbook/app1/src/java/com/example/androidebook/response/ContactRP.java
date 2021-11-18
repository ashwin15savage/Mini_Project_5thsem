package com.example.androidebook.response;

import com.example.androidebook.item.ContactList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ContactRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("name")
    private String name;

    @SerializedName("email")
    private String email;

    @SerializedName("contact_list")
    private List<ContactList> contactLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<ContactList> getContactLists() {
        return contactLists;
    }
}
