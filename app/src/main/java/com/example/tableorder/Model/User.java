package com.example.tableorder.Model;

public class User {
    private String Name, Password, Email, Phone, IsStaff, secureCode;

    public User(){

    }

    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;
    }

    public User (String name, String password, String email, String phone, String secureCode){
        Name = name;
        Password = password;
        Email = email;
        Phone = phone;
        this.secureCode = secureCode;
        IsStaff = "false";
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public String getName(){
        return Name;
    }

    public void setName(String name){

        Name =name;
    }

    public  String getPassword()
    {
        return Password;
    }

    public void setPassword(String password){
        Password = password;
    }
    public String getEmail(){
        return Email;
    }
    public void setEmail(String email){
        Email = email;
    }
    public String getPhone(){
        return Phone;
    }
    public void setPhone(String phone){
        Phone = phone;
    }
}
