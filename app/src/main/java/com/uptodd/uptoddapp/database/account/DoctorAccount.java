package com.uptodd.uptoddapp.database.account;

public class DoctorAccount {
    private int id;
    private String name;
    private String mail;
    private String phone;
    private String whatsapp;
    private String address;
    private String password;
    private String bankAccountNo;
    private String bankName;
    private String ifscCode;
    private String accountHolderName;
    private String ip;
    private int isReferred;
    private int totalPatientReferred;
    private int totalPatientEnrolled;
    private float amountByReferringPatient;
    private int totalDoctorReferred;
    private int totalDoctorEnrolled;
    private float amountByReferringDoctor;
    private float amountInBank;
    private float pendingAmount;


    // Getter Methods


    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMail() {
        return mail;
    }

    public String getPhone() {
        return phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public String getAddress() {
        return address;
    }

    public String getPassword() {
        return password;
    }

    public String getBankAccountNo() {
        return bankAccountNo;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getIp() {
        return ip;
    }

    public int getIsReferred() {
        return isReferred;
    }

    public int getTotalPatientReferred() {
        return totalPatientReferred;
    }

    public int getTotalPatientEnrolled() {
        return totalPatientEnrolled;
    }

    public float getAmountByReferringPatient() {
        return amountByReferringPatient;
    }

    public int getTotalDoctorReferred() {
        return totalDoctorReferred;
    }

    public int getTotalDoctorEnrolled() {
        return totalDoctorEnrolled;
    }

    public float getAmountByReferringDoctor() {
        return amountByReferringDoctor;
    }

    public float getAmountInBank() {
        return amountInBank;
    }

    public float getPendingAmount() {
        return pendingAmount;
    }

    // Setter Methods

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setIsReferred(int isReferred) {
        this.isReferred = isReferred;
    }

    public void setTotalPatientReferred(int totalPatientReferred) {
        this.totalPatientReferred = totalPatientReferred;
    }

    public void setTotalPatientEnrolled(int totalPatientEnrolled) {
        this.totalPatientEnrolled = totalPatientEnrolled;
    }

    public void setAmountByReferringPatient(float amountByReferringPatient) {
        this.amountByReferringPatient = amountByReferringPatient;
    }

    public void setTotalDoctorReferred(int totalDoctorReferred) {
        this.totalDoctorReferred = totalDoctorReferred;
    }

    public void setTotalDoctorEnrolled(int totalDoctorEnrolled) {
        this.totalDoctorEnrolled = totalDoctorEnrolled;
    }

    public void setAmountByReferringDoctor(float amountByReferringDoctor) {
        this.amountByReferringDoctor = amountByReferringDoctor;
    }

    public void setAmountInBank(float amountInBank) {
        this.amountInBank = amountInBank;
    }

    public void setPendingAmount(float pendingAmount) {
        this.pendingAmount = pendingAmount;
    }

    public float getTotalAmount() {
        return this.amountByReferringDoctor + this.amountByReferringPatient + this.amountInBank + this.pendingAmount;
    }

    public DoctorAccount(){}

    //copy constructor
    public DoctorAccount(DoctorAccount anotherDoctorAccount){
        this.id = anotherDoctorAccount.id;
        this.name = anotherDoctorAccount.name;
        this.mail = anotherDoctorAccount.mail;
        this.phone = anotherDoctorAccount.phone;
        this.whatsapp = anotherDoctorAccount.whatsapp;
        this.address = anotherDoctorAccount.address;
        this.password = anotherDoctorAccount.password;
        this.bankAccountNo = anotherDoctorAccount.bankAccountNo;
        this.ifscCode = anotherDoctorAccount.ifscCode;
        this.accountHolderName = anotherDoctorAccount.accountHolderName;
        this.bankName = anotherDoctorAccount.bankName;
        this.ip = anotherDoctorAccount.ip;
        this.isReferred = anotherDoctorAccount.isReferred;
        this.totalPatientReferred = anotherDoctorAccount.totalPatientReferred;
        this.totalPatientEnrolled = anotherDoctorAccount.totalPatientEnrolled;
        this.amountByReferringPatient = anotherDoctorAccount.amountByReferringPatient;
        this.totalDoctorReferred = anotherDoctorAccount.totalDoctorReferred;
        this.totalDoctorEnrolled = anotherDoctorAccount.totalDoctorEnrolled;
        this.amountByReferringDoctor = anotherDoctorAccount.amountByReferringDoctor;
        this.amountInBank = anotherDoctorAccount.amountInBank;
        this.pendingAmount = anotherDoctorAccount.pendingAmount;
    }
}
