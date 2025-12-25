package com.example.timeflow.requestandresponse;

public class UserRegisterRequest {
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;  // 可以是头像的URL或base64编码

    // 构造函数
    public UserRegisterRequest() {}

    public UserRegisterRequest(String username, String password, String email, String nickname, String avatar) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.nickname = nickname;
        this.avatar = avatar;
    }

    // Getter 和 Setter 方法
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}