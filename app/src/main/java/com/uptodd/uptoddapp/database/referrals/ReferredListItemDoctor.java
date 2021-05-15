package com.uptodd.uptoddapp.database.referrals;

import java.io.Serializable;

//Class for referred list item
public class ReferredListItemDoctor implements Serializable {
//
//    private String name, email, referral_status;
//    private Long referral_date, referral_ID;
//    private boolean paid;
//
//    public Long getReferral_ID() {
//        return referral_ID;
//    }
//
//    public void setReferral_ID(Long referral_ID) {
//        this.referral_ID = referral_ID;
//    }
//
//    public String getName() {
//        return name;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getReferral_status() {
//        return referral_status;
//    }
//
//    public void setReferral_status(String referral_status) {
//        this.referral_status = referral_status;
//    }
//
//    public Long getReferral_date() {
//        return referral_date;
//    }
//
//    public void setReferral_date(Long  referral_date) {
//        this.referral_date = referral_date;
//    }
//
//    public boolean isPaid() {
//        return paid;
//    }
//
//    public void setPaid(boolean paid) {
//        this.paid = paid;
//    }
//
//    public ReferredListItem(Long referral_ID,String name, String email, String referral_status, Long  referral_date, boolean paid) {
//        this.referral_ID = referral_ID;
//        this.name = name;
//        this.email = email;
//        this.referral_status = referral_status;
//        this.referral_date = referral_date;
//        this.paid = paid;
//    }
//
//    public ReferredListItem(){
//
//    }

    private int id;
    private int doctorId;
    private String name;
    private String mail;
    private String phone;
    private String city;
    private String referralDate;
    private long referralDateValue;
    private String registrationDate;
    private long registrationDateValue;
    private String referralStatus;
    private String doctorFeedback;
    private String feedbackdates;
    private long feedbackdatesValue;
    private float totalReferred;
    float totalAmountEarned;
    private String ip;

    public float getTotalReferred() {
        return totalReferred;
    }

    public void setTotalReferred(float totalReferred) {
        this.totalReferred = totalReferred;
    }

    public float getTotalAmountEarned() {
        return totalAmountEarned;
    }

    public void setTotalAmountEarned(float totalAmountEarned) {
        this.totalAmountEarned = totalAmountEarned;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getId() {
        return id;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public long getReferralDateValue() {
        return referralDateValue;
    }

    public void setReferralDateValue(long referralDateValue) {
        this.referralDateValue = referralDateValue;
    }

    public long getRegistrationDateValue() {
        return registrationDateValue;
    }

    public void setRegistrationDateValue(long registrationDateValue) {
        this.registrationDateValue = registrationDateValue;
    }

    public long getFeedbackdatesValue() {
        return feedbackdatesValue;
    }

    public void setFeedbackdatesValue(long feedbackdatesValue) {
        this.feedbackdatesValue = feedbackdatesValue;
    }

    // Getter Methods

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public String getCity() {
        return city;
    }

    public String getReferralDate() {
        return referralDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public String getReferralStatus() {
        return referralStatus;
    }

    public String getDoctorFeedback() {
        return doctorFeedback;
    }

    public String getFeedbackdates() {
        return feedbackdates;
    }

    public String getIp() {
        return ip;
    }

    // Setter Methods

    public void setName(String name) {
        this.name = name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setReferralDate(String referralDate) {
        this.referralDate = referralDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setReferralStatus(String referralStatus) {
        this.referralStatus = referralStatus;
    }

    public void setDoctorFeedback(String doctorFeedback) {
        this.doctorFeedback = doctorFeedback;
    }

    public void setFeedbackdates(String feedbackdates) {
        this.feedbackdates = feedbackdates;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


}
