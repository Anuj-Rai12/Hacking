package com.uptodd.uptoddapp.database.support;

import java.io.Serializable;

public class Ticket implements Serializable {

    private int id;
    private int userId;
    private String ticketNumber;
    private int status;
    private int rating;
    private String subject;
    private String type;
    private String ticketOwner;
    private String ticketCreationDate;
    private long time;
    private int ticketReopenCount;
    private int ticketPriorityLevel;
    private String userIp;
    private String adminIp;
    private String customerFeedback;
    private String message;

    public Ticket(int userId, String ticketNumber, String subject, String type, String message) {
        this.userId = userId;
        this.ticketNumber = ticketNumber;
        this.subject = subject;
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // Getter Methods



    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getTicketNumber() {
        return ticketNumber;
    }

    public int getStatus() {
        return status;
    }

    public int getRating() {
        return rating;
    }

    public String getSubject() {
        return subject;
    }

    public String getType() {
        return type;
    }

    public String getTicketOwner() {
        return ticketOwner;
    }

    public String getTicketCreationDate() {
        return ticketCreationDate;
    }

    public long getTime(){
        return time;
    }

    public int getTicketReopenCount() {
        return ticketReopenCount;
    }

    public int getTicketPriorityLevel() {
        return ticketPriorityLevel;
    }

    public String getUserIp() {
        return userIp;
    }

    public String getAdminIp() {
        return adminIp;
    }

    public String getCustomerFeedback() {
        return customerFeedback;
    }

    // Setter Methods

    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setTicketNumber(String ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTicketOwner(String ticketOwner) {
        this.ticketOwner = ticketOwner;
    }

    public void setTicketCreationDate(String ticketCreationDate) {
        this.ticketCreationDate = ticketCreationDate;
    }

    public void setTime(long time){
        this.time = time;
    }

    public void setTicketReopenCount(int ticketReopenCount) {
        this.ticketReopenCount = ticketReopenCount;
    }

    public void setTicketPriorityLevel(int ticketPriorityLevel) {
        this.ticketPriorityLevel = ticketPriorityLevel;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public void setAdminIp(String adminIp) {
        this.adminIp = adminIp;
    }

    public void setCustomerFeedback(String customerFeedback) {
        this.customerFeedback = customerFeedback;
    }
}
