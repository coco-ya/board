package com.cocoyami.board.mappers;

import com.cocoyami.board.entities.member.EmailAuthEntity;
import com.cocoyami.board.entities.member.UserEntity;
import com.cocoyami.board.vos.ArticleReadVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IMemberMapper {
    // 타입을 int로 한 이유는 레코드의 개수를 가져오기 위함이다.
    int insertUser(UserEntity user);

    int insertEmailAuth(EmailAuthEntity emailAuth);

    // @Param 어노테이션을 사용하게 되면 Mapper에서 parameterType 사용하면 안됨
    // BINARY는 소문자 대문자를 구별하여 검색하는데 사용
    UserEntity selectUserByEmail(@Param(value = "email") String Email);

    EmailAuthEntity selectEmailAuthByEmailCodeSalt(@Param(value = "email") String email,
                                                   @Param(value = "code")String code,
                                                   @Param(value = "salt")String salt);

    UserEntity selectUserByNickname(@Param(value = "nickname") String nickname);

    UserEntity selectUserByContact(@Param(value = "contact") String contact);

    UserEntity selectUserByEmailPassword(@Param(value = "email")String email,
                                         @Param(value = "password")String password);

    int updateEmailAuth(EmailAuthEntity emailAuth);



}
