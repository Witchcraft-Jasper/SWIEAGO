package com.example.myguideapplication.construction;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter

public class User {
    private static User user = new User();
    private Integer id;
    private String username;
    private String password;
    private String Email;
    private int VIP;
    private String phone;
    private byte[] profile;

    public User() {
    }

    public User(Integer id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(Integer id, String username, String password, String email, int VIP, String phone,byte[] profile) {
        this.id = id;
        this.username = username;
        this.password = password;
        Email = email;
        this.VIP = VIP;
        this.phone = phone;
        this.profile = profile;
    }

    public User(Integer id, String username, String password, String email, int VIP, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        Email = email;
        this.VIP = VIP;
        this.phone = phone;
    }

    public User(String name, String password, String email, int VIP, String phone) {
        this.username = name;
        this.password = password;
        Email = email;
        this.VIP = VIP;
        this.phone = phone;
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User temp){
        user = temp;
    }
}
