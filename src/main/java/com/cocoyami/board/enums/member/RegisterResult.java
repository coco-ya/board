package com.cocoyami.board.enums.member;

import com.cocoyami.board.interfaces.IResult;

public enum RegisterResult implements IResult {
    EMAIL_NOT_VERIFIED, //이메일 확인 불가
    OVERLAP_CONTACT //연락처 중복
}
