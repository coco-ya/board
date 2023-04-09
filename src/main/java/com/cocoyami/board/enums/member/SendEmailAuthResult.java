package com.cocoyami.board.enums.member;

import com.cocoyami.board.interfaces.IResult;

public enum SendEmailAuthResult implements IResult {
    EMAIL_DUPLICATED, //이메일 중복
    EXPIRED //인증시간 만료
}
