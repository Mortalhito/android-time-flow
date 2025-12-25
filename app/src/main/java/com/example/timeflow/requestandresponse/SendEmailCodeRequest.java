package com.example.timeflow.requestandresponse;

public class SendEmailCodeRequest {
    private String toAddress;
    private EmailCodeScene scene; // "REGISTER", "RESET_PASSWORD", "BIND_EMAIL"

    public SendEmailCodeRequest() {}

    public SendEmailCodeRequest(String toAddress, EmailCodeScene scene) {
        this.toAddress = toAddress;
        this.scene = scene;
    }

    public EmailCodeScene getScene() {
        return scene;
    }

    public void setScene(EmailCodeScene scene) {
        this.scene = scene;
    }



    // getter和setter
    public String getToAddress() { return toAddress; }
    public void setToAddress(String toAddress) { this.toAddress = toAddress; }

}
