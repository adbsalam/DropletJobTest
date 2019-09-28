package com.salam.dropletjobtest.user_Model;

/**
 * Constructor to get and set data from or to Firebase
 */
public class UsersModel {

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }

    public void setCurrentAddress(String currentAddress) {
        this.currentAddress = currentAddress;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    /**
     *
     * @param id ID of user
     * @param fullName fullname of user
     * @param currentAddress current address of user
     * @param about about info user
     * @param imageURL imageURL of the user
     */
    public UsersModel(String id, String fullName, String currentAddress, String about, String imageURL, String email) {
        this.id = id;
        this.fullName = fullName;
        this.currentAddress = currentAddress;
        this.about = about;
        this.imageURL = imageURL;
        this.email = email;
    }

    private String id;
    private String fullName;
    private String currentAddress;
    private String about;
    private String imageURL;
    private String email;

    public UsersModel() {
    }


}
