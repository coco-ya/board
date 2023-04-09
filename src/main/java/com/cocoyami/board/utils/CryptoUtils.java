package com.cocoyami.board.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CryptoUtils {
    //암호화 및 복호화를 수행하기 위한 유틸리티 클래스
    //암호화 및 복호화를 수행할 수 있는 메소드를 제공

    public static String hasSha512(String input) {
        try {
            //StringBuilder :
            StringBuilder passwordSaltHashBuilder = new StringBuilder();
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(input.getBytes(StandardCharsets.UTF_8));
            for (byte hashByte : md.digest()) {
                passwordSaltHashBuilder.append(String.format("%02x", hashByte));
            }
            return passwordSaltHashBuilder.toString();
        } catch (NoSuchAlgorithmException ignored) {
            return null;
        }

    }
    private CryptoUtils() {
        //생성자를 만들어서 객체화를 못하게 함
        //하는 이유 : 클래스의 인스턴스를 직접 생성하는 것을 방지하고, 클래스 내부에서만 인스턴스를 생성하도록 제한하기 위해서
        //클래스의 인스턴스를 직접 생성하는 것을 방지하면, 프로그램의 안정성과 보안성을 향상시킬 수 있음
    }

}
