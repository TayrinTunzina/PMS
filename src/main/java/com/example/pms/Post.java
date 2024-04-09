package com.example.pms;

public class Post {
    private String postText;
    private String postImageSrc;
    private String dates;
    private String nbLikes;
//    private String nbComments;

    public String getPostText() {
        return postText;
    }

    public void setPostText(String Text) {
        this.postText = Text;
    }

    public String getPostImageSrc() {
        return postImageSrc;
    }

    public void setPostImageSrc(String postImageSrc) {
        this.postImageSrc = postImageSrc;
    }

    public String getDates() {
        return dates;
    }

    public void setDates(String dates) {
        this.dates = dates;
    }

    public String getNbLikes() {
        return nbLikes;
    }

    public void setNbLikes(String nbLikes) {
        this.nbLikes = nbLikes;
    }

//    public String getNbComments() {
//        return nbComments;
//    }

//    public void setNbComments(String nbComments) {
//        this.nbComments = nbComments;
//    }
}
