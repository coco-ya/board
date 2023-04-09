package com.cocoyami.board.controllers;

import com.cocoyami.board.entities.member.EmailAuthEntity;
import com.cocoyami.board.entities.member.UserEntity;
import com.cocoyami.board.enums.CommonResult;
import com.cocoyami.board.interfaces.IResult;
import com.cocoyami.board.services.MemberService;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.NoSuchAlgorithmException;

@Controller(value = "com.cocoyami.board.controllers.MemberController")
@RequestMapping(value = "member")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }
    //의존성 주입

    //회원가입
    @RequestMapping(value = "register",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRegister(){
        ModelAndView modelAndView = new ModelAndView("member/register");
        return modelAndView;
    }

    @RequestMapping(value = "register",
    method = RequestMethod.POST,
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(UserEntity user, EmailAuthEntity emailAuth , UserEntity newUser){
        Enum<? extends IResult> result = this.memberService.register(user, emailAuth, newUser);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result",result.name().toLowerCase());
        return responseObject.toString();
    }

    //이메일 인증
    @RequestMapping(value = "email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postEmail(UserEntity user, EmailAuthEntity emailAuth, HttpServletRequest request) throws NoSuchAlgorithmException, MessagingException {
        Enum<? extends IResult> result = this.memberService.sendEmailAuth(user, emailAuth, request);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        if (result == CommonResult.SUCCESS) {
            responseObject.put("salt", emailAuth.getSalt());
        }
        return responseObject.toString();
    }

    @RequestMapping(value = "email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchEmail(EmailAuthEntity emailAuth) {
        Enum<?> result = this.memberService.verifyEmailAuth(emailAuth);
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }


    @RequestMapping(value = "registerCom",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getRegisterCom(){
        ModelAndView modelAndView = new ModelAndView("member/registerCom");
        return modelAndView;
    }

    //로그인
    @RequestMapping(value = "login",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getLogin(){
        ModelAndView modelAndView = new ModelAndView("member/login");
        return modelAndView;
    }

    @RequestMapping(value = "login",
            method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(HttpSession session, UserEntity user) {

        Enum<?> result = this.memberService.login(user);
        JSONObject responseObject = new JSONObject();
        //String 을 받아야하면 JSON
        responseObject.put("result", result.name().toLowerCase());

        if (result != CommonResult.SUCCESS) {
            System.out.println("실패");
        }
        session.setAttribute("user", user);
        //세선 저장소로 부터 값을 불러온다.
        System.out.println("성공");
        return responseObject.toString();
    }

    //로그아웃
    @RequestMapping(value = "logout",
            method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getLogout(HttpSession session) {
        session.setAttribute("user", null);
        //user 값이 null 이 아니면 로그인이 된거 - user가 쿠키값인거 같음
        //user 값이 null 이면 로그아웃
        ModelAndView modelAndView = new ModelAndView("redirect:login");
        //로그인 페이지로 리다이랙션(다시 돌아간다)
        //주소에 login 대신 logout 넣으면 다시 login 페이지로 넘어가고 상단바에 로그아웃이랑 마이페이지 없어짐
        return modelAndView;
    }


}
