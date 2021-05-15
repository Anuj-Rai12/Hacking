package com.uptodd.uptoddapp.database.support;

public class Sessions {

//    private String sessionTitle, sessionMessage, sessionID, sessionStatus, supportType;
//    private Integer sessionRating;
//    private Long time;
//
//    public Sessions(){}
//
//    public Sessions(String sessionTitle, String sessionID, String sessionStatus, String supportType, Long time) {
//        this.sessionTitle = sessionTitle;
//        this.sessionID = sessionID;
//        this.sessionStatus = sessionStatus;
//        this.time = time;
//        this.supportType = supportType;
//    }
//
//    public Sessions(String sessionTitle, String sessionMessage, String sessionID, String sessionStatus, String supportType, Integer sessionRating, Long time) {
//        this.sessionTitle = sessionTitle;
//        this.sessionMessage = sessionMessage;
//        this.sessionID = sessionID;
//        this.sessionStatus = sessionStatus;
//        this.supportType = supportType;
//        this.sessionRating = sessionRating;
//        this.time = time;
//    }
//
//    public String getSessionTitle() {
//        return sessionTitle;
//    }
//
//    public void setSessionTitle(String sessionTitle) {
//        this.sessionTitle = sessionTitle;
//    }
//
//    public String getSessionMessage() {
//        return sessionMessage;
//    }
//
//    public void setSessionMessage(String sessionMessage) {
//        this.sessionMessage = sessionMessage;
//    }
//
//    public String getSessionID() {
//        return sessionID;
//    }
//
//    public void setSessionID(String sessionID) {
//        this.sessionID = sessionID;
//    }
//
//    public String getSessionStatus() {
//        return sessionStatus;
//    }
//
//    public void setSessionStatus(String sessionStatus) {
//        this.sessionStatus = sessionStatus;
//    }
//
//    public String getSupportType() {
//        return supportType;
//    }
//
//    public void setSupportType(String supportType) {
//        this.supportType = supportType;
//    }
//
//    public Integer getSessionRating() {
//        return sessionRating;
//    }
//
//    public void setSessionRating(Integer sessionRating) {
//        this.sessionRating = sessionRating;
//    }
//
//    public Long getTime() {
//        return time;
//    }
//
//    public void setTime(Long time) {
//        this.time = time;
//    }

    private int id;
    private int userId;
    private int expertId;
    private String sessionBookingDate;
    private long sessionBookingDateValue;
    private String sessionDateSelected;
    private long sessionDateSelectedValue;
    private String sessionTopic;
    private String expertName;
    private String sessionFeedback;
    private int sessionRating;
    private String bestTimeForSession;
    private int sessionStatus;

    public long getSessionBookingDateValue() {
        return sessionBookingDateValue;
    }

    public void setSessionBookingDateValue(long sessionBookingDateValue) {
        this.sessionBookingDateValue = sessionBookingDateValue;
    }


    // Getter Methods

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getSessionBookingDate() {
        return sessionBookingDate;
    }

    public String getSessionDateSelected() {
        return sessionDateSelected;
    }

    public String getSessionTopic() {
        return sessionTopic;
    }

    public int getExpertId() {
        return expertId;
    }

    public String getExpertName() {
        return expertName;
    }

    public String getSessionFeedback() {
        return sessionFeedback;
    }

    public int getSessionRating() {
        return sessionRating;
    }

    public String getBestTimeForSession() {
        return bestTimeForSession;
    }

    public int getSessionStatus() {
        return sessionStatus;
    }

    // Setter Methods 

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setSessionBookingDate(String sessionBookingDate) {
        this.sessionBookingDate = sessionBookingDate;
    }

    public void setSessionDateSelected(String sessionDateSelected) {
        this.sessionDateSelected = sessionDateSelected;
    }

    public void setSessionTopic(String sessionTopic) {
        this.sessionTopic = sessionTopic;
    }

    public void setExpertId(int expertId) {
        this.expertId = expertId;
    }

    public void setExpertName(String expertName) {
        this.expertName = expertName;
    }

    public void setSessionFeedback(String sessionFeedback) {
        this.sessionFeedback = sessionFeedback;
    }

    public void setSessionRating(int sessionRating) {
        this.sessionRating = sessionRating;
    }

    public void setBestTimeForSession(String bestTimeForSession) {
        this.bestTimeForSession = bestTimeForSession;
    }

    public void setSessionStatus(int sessionStatus) {
        this.sessionStatus = sessionStatus;
    }

}
