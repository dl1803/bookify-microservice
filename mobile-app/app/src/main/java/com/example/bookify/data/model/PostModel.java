package com.example.bookify.data.model;

public class PostModel {
    private String userName;
    private String postTime;
    private String postContent;
    private String bookTitle;
    private String bookAuthor;
    private int userAvatarResId;
    private int bookCoverResId;
    private int likesCount;
    private int commentsCount;
    private boolean isLiked;

    public PostModel(String userName, String postTime, String postContent, String bookTitle, String bookAuthor, int userAvatarResId, int bookCoverResId, int likesCount, int commentsCount) {
        this.userName = userName;
        this.postTime = postTime;
        this.postContent = postContent;
        this.bookTitle = bookTitle;
        this.bookAuthor = bookAuthor;
        this.userAvatarResId = userAvatarResId;
        this.bookCoverResId = bookCoverResId;
        this.likesCount = likesCount;
        this.commentsCount = commentsCount;
        this.isLiked = false;
    }

    public String getUserName() {
        return userName;
    }

    public String getPostTime() {
        return postTime;
    }

    public String getPostContent() {
        return postContent;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public String getBookAuthor() {
        return bookAuthor;
    }

    public int getUserAvatarResId() {
        return userAvatarResId;
    }

    public int getBookCoverResId() {
        return bookCoverResId;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int likesCount) {
        this.likesCount = likesCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        this.isLiked = liked;
    }

    public static final int TYPE_REVIEW = 0;
    public static final int TYPE_QUOTE = 1;
    public static final int TYPE_PROGRESS = 2;

    private int postType = TYPE_REVIEW;
    private float rating = 5.0f;
    private int currentPage = 0;
    private int totalPages = 0;

    public void setPostContent(String postContent) {
        this.postContent = postContent;
    }

    private boolean isQuote;
    private String quoteSource;
    private boolean isSaved;

    public boolean isQuote() {
        return isQuote || postType == TYPE_QUOTE;
    }

    public void setQuote(boolean quote) {
        isQuote = quote;
        if (quote) postType = TYPE_QUOTE;
    }

    public String getQuoteSource() {
        return quoteSource;
    }

    public void setQuoteSource(String quoteSource) {
        this.quoteSource = quoteSource;
    }

    public boolean isSaved() {
        return isSaved;
    }

    public void setSaved(boolean saved) {
        isSaved = saved;
    }

    public int getPostType() {
        return postType;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
}
