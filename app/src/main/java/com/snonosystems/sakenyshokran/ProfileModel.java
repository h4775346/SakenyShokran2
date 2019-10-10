package com.snonosystems.sakenyshokran;




public class ProfileModel {

private String uid,name,email,phone,image;


    public ProfileModel(String uid, String name, String email, String phone, String image) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.image = image;
    }
    public void clear(){

        this.uid = null;
        this.email = null;
        this.name = "default";
        this.phone = "default";
        this.image = "default";

    }

    public ProfileModel() {

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
