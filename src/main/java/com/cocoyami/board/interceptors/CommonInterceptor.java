package com.cocoyami.board.interceptors;

import com.cocoyami.board.entities.board.BoardEntity;
import com.cocoyami.board.services.BoardService;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CommonInterceptor implements HandlerInterceptor {
    @Resource // 이거 써서 bbsService 에 접근 가능
    private BoardService boardService; //멤버 변수

    @Override //들어오기 전 처리하는게 preHandle
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        BoardEntity[] boards = this.boardService.getBoards();
        request.setAttribute("boards",boards); //body.html 에 th:each 로 추가하면 여러개 안쓰고 하나만 써도됨
        return true;
    }
    //HttpServletRequest : 요청에 대한 정보
    //HttpServletResponse : 응답에 대한 정보
    //return 이 true면 요청이 컨트롤러로 넘어감

}
