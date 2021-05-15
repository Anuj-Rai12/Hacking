package com.uptodd.uptoddapp.support.view;

import androidx.annotation.Nullable;

public class TicketMessage {
//    private Integer messageId;
//    private String messageText;
//    private Long messageTime;
//    private Integer messageStatus;
//    private boolean sender;
//
//    public Long getMessageTime() {
//        return messageTime;
//    }
//
//    public void setMessageTime(Long messageTime) {
//        this.messageTime = messageTime;
//    }
//
//    public Integer getMessageStatus() {
//        return messageStatus;
//    }
//
//    public void setMessageStatus(Integer messageStatus) {
//        this.messageStatus = messageStatus;
//    }
//
//    public TicketMessage(){}
////
////    public TicketMessage(Integer messageId, String messageText, boolean sender) {
////        this.messageId = messageId;
////        this.messageText = messageText;
////        this.sender = sender;
////    }
//
//    public TicketMessage(Integer messageId, String messageText, Long messageTime, Integer messageStatus, boolean sender) {
//        this.messageId = messageId;
//        this.messageText = messageText;
//        this.messageTime = messageTime;
//        this.messageStatus = messageStatus;
//        this.sender = sender;
//    }
//
//    public boolean isSender() {
//        return sender;
//    }
//
//    public void setSender(boolean sender) {
//        this.sender = sender;
//    }
//
//    @Override
//    public boolean equals(@Nullable Object obj) {
//        return super.equals(obj);
//    }
//
//    public Integer getMessageId() {
//        return messageId;
//    }
//
//    public void setMessageId(Integer messageId) {
//        this.messageId = messageId;
//    }
//
//    public String getMessageText() {
//        return messageText;
//    }
//
//    public void setMessageText(String messageText) {
//        this.messageText = messageText;
//    }

    private String id = null;
    private String message;
    private String timestamp;
    private long time;
    private String sender;
    private boolean senderValue;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSenderValue() {
        return senderValue;
    }

    public void setSenderValue(boolean senderValue) {
        this.senderValue = senderValue;
    }
// Getter Methods

    public String getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getSender() {
        return sender;
    }

    // Setter Methods

    public void setId(String id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    public TicketMessage(String message, long time, String sender, boolean senderValue) {
        this.message = message;
        this.time = time;
        this.sender = sender;
        this.senderValue = senderValue;
    }
}
