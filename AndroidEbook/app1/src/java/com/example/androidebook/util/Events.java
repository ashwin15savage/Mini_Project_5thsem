package com.example.androidebook.util;

import com.example.androidebook.item.CommentList;

import java.util.List;

public class Events {

    // Event used to send message from login notify.
    public static class Login {
        private String login;

        public Login(String login) {
            this.login = login;
        }

        public String getLogin() {
            return login;
        }
    }

    // Event used to send message from comment notify.
    public static class AddComment {

        private String comment_id, book_id, user_id, user_name, user_image, comment_text, comment_date, total_comment;

        public AddComment(String comment_id, String book_id, String user_id, String user_name, String user_image, String comment_text, String comment_date, String total_comment) {
            this.comment_id = comment_id;
            this.book_id = book_id;
            this.user_id = user_id;
            this.user_name = user_name;
            this.user_image = user_image;
            this.comment_text = comment_text;
            this.comment_date = comment_date;
            this.total_comment = total_comment;
        }

        public String getComment_id() {
            return comment_id;
        }

        public String getBook_id() {
            return book_id;
        }

        public String getUser_id() {
            return user_id;
        }

        public String getUser_name() {
            return user_name;
        }

        public String getUser_image() {
            return user_image;
        }

        public String getComment_text() {
            return comment_text;
        }

        public String getComment_date() {
            return comment_date;
        }

        public String getTotal_comment() {
            return total_comment;
        }

    }

    //Event used to delete comment notify and total comment.
    public static class DeleteComment {

        private String totalComment, bookId, type;
        private List<CommentList> commentLists;

        public DeleteComment(String totalComment, String bookId, String type, List<CommentList> commentLists) {
            this.totalComment = totalComment;
            this.bookId = bookId;
            this.type = type;
            this.commentLists = commentLists;
        }

        public String getTotalComment() {
            return totalComment;
        }

        public String getBookId() {
            return bookId;
        }

        public String getType() {
            return type;
        }

        public List<CommentList> getCommentLists() {
            return commentLists;
        }
    }

    //Event used to update profile
    public static class ProfileUpdate {

        private String string;

        public ProfileUpdate(String string) {
            this.string = string;
        }

        public String getString() {
            return string;
        }
    }

    //Event used to update remove and update image
    public static class ProImage {

        private String string, imagePath;
        private boolean isProfile, isRemove;

        public ProImage(String string, String imagePath, boolean isProfile, boolean isRemove) {
            this.string = string;
            this.imagePath = imagePath;
            this.isProfile = isProfile;
            this.isRemove = isRemove;
        }

        public String getString() {
            return string;
        }

        public String getImagePath() {
            return imagePath;
        }

        public boolean isProfile() {
            return isProfile;
        }

        public boolean isRemove() {
            return isRemove;
        }
    }

}
