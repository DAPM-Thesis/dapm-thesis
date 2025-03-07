package model;

import java.util.Date;

public class Event {

    private String caseId;
    private String activity;
    private Date timeStamp;

    public Event() {}

    public Event(String caseId, String activity, Date timeStamp) {
        this.caseId = caseId;
        this.activity = activity;
        this.timeStamp = timeStamp;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {
        return "Event{" + '\n' +
                "caseId= " + caseId + '\n' +
                "activity= " + activity + '\n' +
                "timeStamp= " + timeStamp + '\n' +
                '}';
    }
}