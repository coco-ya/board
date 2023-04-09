package com.cocoyami.board.vos;

import com.cocoyami.board.entities.board.CommentEntity;

public class CommentVo extends CommentEntity {
    private  String userNickname;
    private boolean isSigned; // Vo는 테이블에 없는거 써도됨

    private boolean isMine; //이 댓글이 내꺼냐

    private boolean isLiked;

    private int likeCount; //좋아요 수



    public int getLikeCount() {
        return likeCount;
    }

    public CommentVo setLikeCount(int likeCount) {
        this.likeCount = likeCount;
        return this;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public CommentVo setLiked(boolean liked) {
        isLiked = liked;
        return this;
    }

    public boolean isMine() {
        return isMine;
    }

    public CommentVo setMine(boolean mine) {
        isMine = mine;
        return this;
    }

    public String getUserNickname(){
        return userNickname;
    }

    public void setUserNickname(String userNickname){
        this.userNickname=userNickname;
    }

    public boolean isSigned() {
        return isSigned;
    }

    public CommentVo setSigned(boolean signed) {
        isSigned = signed;
        return this;
    }
}
