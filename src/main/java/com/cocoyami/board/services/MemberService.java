package com.cocoyami.board.services;

import com.cocoyami.board.entities.member.EmailAuthEntity;
import com.cocoyami.board.entities.member.UserEntity;
import com.cocoyami.board.enums.CommonResult;
import com.cocoyami.board.enums.member.OverlapResult;
import com.cocoyami.board.enums.member.RegisterResult;
import com.cocoyami.board.enums.member.SendEmailAuthResult;
import com.cocoyami.board.interfaces.IResult;
import com.cocoyami.board.mappers.IMemberMapper;
import com.cocoyami.board.utils.CryptoUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

@Service(value = "com.cocoyami.board.services.MemberService")
public class MemberService {
    private final IMemberMapper memberMapper;

    //스프링 프레임워크에서 제공되는 인터페이스, 스프링에서 메일 전 기능을 구현하기 위한 핵심적 인터페이스 중 하나
    private final JavaMailSender mailSender;

    //템플릿 엔진을 추상화한 인터페이스, 스프링에서는 Thymeleaf, Freemarker, Velocity 등 다양한 템플릿 엔진을 지원
    private final TemplateEngine templateEngine;


    @Autowired
    public MemberService(IMemberMapper memberMapper, JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.memberMapper = memberMapper;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    //회원가입 기능
    //Enum : 열거형을 구현하기 위한 자바 언어 키워드, 한정된 개수의 상수를 정의하고자 사용
    public Enum<? extends IResult> register(UserEntity user, EmailAuthEntity emailAuth, UserEntity newUser) {
        // user : DB에 있는 원래 User 값
        // newUser : 웹에서 새로 들어온 User 값
        EmailAuthEntity existingEmailAuth = this.memberMapper.selectEmailAuthByEmailCodeSalt(emailAuth.getEmail(), emailAuth.getCode(), emailAuth.getSalt());

        if (existingEmailAuth == null || emailAuth.IsExpired()) {
            //DB에 값이 없거나 인증 시간이 만료되었다면
            return RegisterResult.EMAIL_NOT_VERIFIED;
        }
        //닉네임 중복 검사
        UserEntity userByNickname = this.memberMapper.selectUserByNickname(newUser.getNickname());
        if (userByNickname != null && !user.getEmail().equals(newUser.getEmail())) {
            //DB에 있는 값이랑 입력된 닉네임이랑 같은데
            //이메일이 서로 다르다 -> 닉네임 중복
            return OverlapResult.OVERLAP_NICKNAME;
        }
        //전화번호 중복 검사
        UserEntity userByContact = this.memberMapper.selectUserByContact(newUser.getContact());
        if (userByContact != null && !user.getEmail().equals(newUser.getEmail())) {
            return OverlapResult.OVERLAP_CONTACT;
        }

        user.setPassword(CryptoUtils.hasSha512(user.getPassword()));
        if (this.memberMapper.insertUser(user) == 0) {
            return CommonResult.FAILURE;
        }
        return CommonResult.SUCCESS;
    }

    //로그인 기능
    @Transactional
    public Enum<? extends IResult> login(UserEntity user) {
        UserEntity existingUser = this.memberMapper.selectUserByEmailPassword(
                user.getEmail(),
                CryptoUtils.hasSha512(user.getPassword()));

        if (existingUser == null) {
            //null이다 -> 입력된 값이랑 원래있던 값이랑 같지않다.
            return CommonResult.FAILURE;
        }
        user.setPassword(existingUser.getPassword());
        user.setName(existingUser.getName());
        user.setNickname(existingUser.getNickname());
        user.setContact(existingUser.getContact());
        user.setAddressPostal(existingUser.getAddressPostal());
        user.setAddressPrimary(existingUser.getAddressPrimary());
        user.setAddressSecondary(existingUser.getAddressSecondary());
        user.setRegisteredOn(existingUser.getRegisteredOn());

        return CommonResult.SUCCESS;
    }

    @Transactional
    // 인증 절차중 중간에 실패를 할경우 다시 처음으로 돌아가게 하는 어노테이션
    public Enum<? extends IResult> sendEmailAuth(UserEntity user, EmailAuthEntity emailAuth, HttpServletRequest request) throws NoSuchAlgorithmException, MessagingException {

        UserEntity existingUser = this.memberMapper.selectUserByEmail(user.getEmail());

        if (existingUser != null) {
            // null이 아니라는거는 이미 사용중인 이메일이라는 뜻
            return SendEmailAuthResult.EMAIL_DUPLICATED; //널이 아니면 중복이라는 뜻
        }
        String authCode = RandomStringUtils.randomNumeric(6);
        String authSalt = String.format("%s%s%f%f",
                user.getEmail(),
                authCode,
                Math.random(),
                Math.random());
        StringBuilder authSaltHashBuilder = new StringBuilder();
        // StringBuilder 타입의 authSaltHashBuilder 변수를 새객체로 생성.
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        //암호화해줄라고 하는거
        // SHA-512는 생성된 고유값이 512비트이다. 128자리 문자열을 반환한다.
        //아래에서 바이트로 처리를 해준다. 왜냐하면 기존에는 비트이기 때문이다.
        md.update(authSalt.getBytes(StandardCharsets.UTF_8));
        //StandardCharsets 은 UTF-8 쓸려면 넣어야함
        // md의 update는 바이트 단위만 받기 때문에 위에서 authSalt를 변경시켜준다.
        // 이때 반드시 UTF-8로 된 getBytes메서드를 사용한다.
        for (byte hashByte : md.digest()) {
            // .digest()는 바이트배열로 해쉬를 반환
            authSaltHashBuilder.append(String.format("%02x", hashByte)); //format : 16진수(%x)로 형식 설정
            // System.out.println(authSaltHashBuilder);
        }
        authSalt = authSaltHashBuilder.toString();

        Date createdOn = new Date();
        // 만료시간이 5분이라서 밑에 DateUtils를 사용하여 5분의 시간을 더해준값으로 명시해준다.
        Date expiresOn = DateUtils.addMinutes(createdOn, 5);
        // createdOn 이 실행된시간 expiresOn이 만료시간
        emailAuth.setCode(authCode);// 랜덤한 6자리 변수
        emailAuth.setSalt(authSalt);// String.format으로 해싱(암호화)된 salt -> salt는 암호화할라고 쓰는거 없으면 password 암호화 안됨
        emailAuth.setEmail(user.getEmail());//user의 이메일
        emailAuth.setCreatedOn(createdOn);
        emailAuth.setExpiresOn(expiresOn);// 위에서 새로 지정한 5분이 추가된 변수
        emailAuth.setIsExpired(false); // 기본값을 false로 명시.
        if (this.memberMapper.insertEmailAuth(emailAuth) == 0) {
            return CommonResult.FAILURE; // 어떠한 이유에 의해서 실패(중복은 아님 위에서 걸러지기 때문에)
        }
        Context context = new Context();
        //Service에서 html파일에 view를 넘겨줘야 하기 때문에 (서비스에서 html파일을 활용하기 위해서)
        context.setVariable("code", emailAuth.getCode());
        context.setVariable("domain", String.format("%s://%s:%d",
                request.getScheme(),
                request.getServerName(),
                request.getServerPort()));

        String text = this.templateEngine.process("member/registerEmailAuth", context);
        // 앞에는 template(setViewName과 같은) 뒤에는 위에서 생성한 code를 변수로 지정한것.
        MimeMessage mail = this.mailSender.createMimeMessage(); //MimeMessage = 메일 보내려고 쓰는거
        MimeMessageHelper helper = new MimeMessageHelper(mail, "UTF-8");
        //MimeMessageHelper = 메일내용에 이미지나 첨부파일 같은거 같이 보낼려고 쓰는거
        helper.setFrom("guswl0490111@gmail.com"); //보내는 사람
        helper.setTo(user.getEmail()); //받는 사람
        helper.setSubject("[COFFEEE] 환영합니다. 회원가입 완료를 위해 확인해주세요.");
        helper.setText(text, true);
        // true를 반드시 작성해야함(html을 사용할것인지에 대한 여부)
        this.mailSender.send(mail);
        return CommonResult.SUCCESS;
    }

    //회원가입 페이지에서 이메일 인증 버튼 눌렀을 때 이메일 인증 버튼 누르고 인증번호 확인까지
    @Transactional
    public Enum<? extends IResult> verifyEmailAuth(EmailAuthEntity emailAuth) {
        EmailAuthEntity existingEmailAuth = this.memberMapper.selectEmailAuthByEmailCodeSalt(
                emailAuth.getEmail(),
                emailAuth.getCode(),
                emailAuth.getSalt());
        if (existingEmailAuth == null) {
            return CommonResult.FAILURE;
        }
        if (existingEmailAuth.getExpiresOn().compareTo(new Date()) < 0) {
            return SendEmailAuthResult.EXPIRED;
        }

        existingEmailAuth.setIsExpired(true);

        if (this.memberMapper.updateEmailAuth(existingEmailAuth) == 0) {
            return CommonResult.FAILURE;
        }
        return CommonResult.SUCCESS;
    }

}
