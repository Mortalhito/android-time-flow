package com.example.timeflow.requestandresponse;

// VerifyCodeRequest.java
public class VerifyCodeRequest {
    private String email;
    private EmailCodeScene scene;
    private String code;

    public VerifyCodeRequest() {}

    public VerifyCodeRequest(String email, EmailCodeScene scene, String code) {
        this.email = email;
        this.scene = scene;
        this.code = code;
    }

    // getter和setter
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public EmailCodeScene getScene() {
        return scene;
    }

    public void setScene(EmailCodeScene scene) {
        this.scene = scene;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
