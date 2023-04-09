package com.cocoyami.board.vos;

import com.cocoyami.board.entities.board.ArticleEntity;

public class ArticleReadVo extends ArticleEntity {
    private String userNickname;

    int commentCount;

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }
}
