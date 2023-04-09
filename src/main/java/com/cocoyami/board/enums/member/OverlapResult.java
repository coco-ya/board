package com.cocoyami.board.enums.member;

import com.cocoyami.board.interfaces.IResult;

public enum OverlapResult implements IResult {
    OVERLAP_CONTACT, //연락처 중복
    OVERLAP_NICKNAME, //닉네임 중복
    OVERLAP_EMAIL //이메일 중복

}
