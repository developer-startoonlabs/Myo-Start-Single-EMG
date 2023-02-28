package com.start.apps.pheezee.pojos;

public class CalbrationData {
    String email_id;
    String date_stamp;
    String time_stamp;

    public CalbrationData(String email_id,String date_stamp, String time_stamp){
        this.email_id = email_id;
        this.date_stamp = date_stamp;
        this.time_stamp = time_stamp;
    }
    public String getemail_id() {
        return email_id;
    }
    public void setemail_id(String email_id) {
        this.email_id = email_id;
    }

    public String gettime_stamp() {
        return time_stamp;
    }

    public void settime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getdate_stamp() {
        return date_stamp;
    }
    public void setdate_stamp(String date_stamp) {
        this.date_stamp = date_stamp;
    }
}
