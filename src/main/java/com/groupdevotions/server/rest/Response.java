package com.groupdevotions.server.rest;

public class Response<T> {
    boolean operationSuccessful;
    LocationType location;
    Message message;
    T data;

    public Response(Response<Object> otherResponse) {
        this.operationSuccessful = otherResponse.operationSuccessful;
        this.location = otherResponse.location;
        this.message = otherResponse.message;
    }

    public Response(T data) {
        this.operationSuccessful = true;
        this.data = data;
    }

    public Response(T data, String infoMessage) {
        this.operationSuccessful = true;
        this.data = data;
        if (infoMessage != null) {
            this.message = new Message(MessageType.info, infoMessage);
        }
    }

    public Response(String errorMessage) {
        this.operationSuccessful = false;
        message = new Message(MessageType.danger, errorMessage);
    }

    public Response(Message message) {
        this.message = message;
        this.operationSuccessful = true;
        if (message != null) {
            this.operationSuccessful = MessageType.success.equals(message.type) || MessageType.info.equals(message.type);
        }
    }

    public Response(String errorMessage, LocationType newLocation) {
        this.operationSuccessful = false;
        message = new Message(MessageType.warning, errorMessage);
        location = newLocation;
    }

    public Response(boolean success, T data, MessageType type, String message, LocationType newLocation) {
        this.operationSuccessful = success;
        this.data = data;
        if (type != null) {
            this.message = new Message(type, message);
        }
        this.location = newLocation;
    }

    public static class Message {
        MessageType type;
        String text;

        public Message(MessageType type, String text) {
            this.type = type;
            this.text = text;
        }
    }

    public static enum MessageType {
        success,
        info,
        warning,
        danger;
    }

    public static enum LocationType {
        checkLogin,
        home,
        login,
        blog,
        devotion,
        terms,
        configureGroup,
        resetPassword,
        groups,
        newOrgAccount,
        settings
    }
}
