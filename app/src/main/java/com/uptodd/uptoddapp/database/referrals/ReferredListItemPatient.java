package com.uptodd.uptoddapp.database.referrals;

public class ReferredListItemPatient {
    private int id;
    private String referredBy;
    private int referredById;
    private String patientName;
    private String patientMail;
    private String patientPhone;
    private String patientWhatsapp;
    private String babyGender;
    private String babyDOB;
    private String referalDate;
    private String babyName;
    private float rating;
    private long referralDateValue;
    private String registrationDate = null;
    private long registrationDateValue;
    private float enrolledDuration;
    private float amountSubmitted;
    private String referralStatus;
    private String feedback;
    private String feedbackdates;
    private long feedbackdatesValue;
    private String ip;

    public String getBabyName() {
        return babyName;
    }

    public void setBabyName(String babyName) {
        this.babyName = babyName;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setReferredById(int referredById) {
        this.referredById = referredById;
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

    public String getReferredBy() {
        return referredBy;
    }

    public float getReferredById() {
        return referredById;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientMail() {
        return patientMail;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public String getPatientWhatsapp() {
        return patientWhatsapp;
    }

    public String getBabyGender() {
        return babyGender;
    }

    public String getBabyDOB() {
        return babyDOB;
    }

    public String getReferalDate() {
        return referalDate;
    }

    public String getRegistrationDate() {
        return registrationDate;
    }

    public float getEnrolledDuration() {
        return enrolledDuration;
    }

    public float getAmountSubmitted() {
        return amountSubmitted;
    }

    public String getReferralStatus() {
        return referralStatus;
    }

    public String getFeedback() {
        return feedback;
    }

    public String getFeedbackdates() {
        return feedbackdates;
    }

    public String getIp() {
        return ip;
    }

    // Setter Methods


    public void setReferredBy(String referredBy) {
        this.referredBy = referredBy;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public void setPatientMail(String patientMail) {
        this.patientMail = patientMail;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public void setPatientWhatsapp(String patientWhatsapp) {
        this.patientWhatsapp = patientWhatsapp;
    }

    public void setBabyGender(String babyGender) {
        this.babyGender = babyGender;
    }

    public void setBabyDOB(String babyDOB) {
        this.babyDOB = babyDOB;
    }

    public void setReferalDate(String referalDate) {
        this.referalDate = referalDate;
    }

    public void setRegistrationDate(String registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void setEnrolledDuration(float enrolledDuration) {
        this.enrolledDuration = enrolledDuration;
    }

    public void setAmountSubmitted(float amountSubmitted) {
        this.amountSubmitted = amountSubmitted;
    }

    public void setReferralStatus(String referralStatus) {
        this.referralStatus = referralStatus;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setFeedbackdates(String feedbackdates) {
        this.feedbackdates = feedbackdates;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
