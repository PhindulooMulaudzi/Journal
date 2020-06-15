package com.phindulo.journal.Util;

import android.app.Application;

public class JournalAPI extends Application {
    private String username;
    private String userID;
    private static JournalAPI journalInstance;

    public static JournalAPI getJournalInstance() {
        if (journalInstance == null)
            journalInstance = new JournalAPI();
        return journalInstance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
