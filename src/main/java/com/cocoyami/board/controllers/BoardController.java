package com.cocoyami.board.controllers;

import com.cocoyami.board.entities.board.ArticleEntity;
import com.cocoyami.board.entities.board.BoardEntity;
import com.cocoyami.board.entities.board.CommentEntity;
import com.cocoyami.board.entities.member.UserEntity;
import com.cocoyami.board.enums.CommonResult;
import com.cocoyami.board.enums.board.WriteResult;
import com.cocoyami.board.models.PagingModel;
import com.cocoyami.board.services.BoardService;
import com.cocoyami.board.vos.ArticleReadVo;
import com.cocoyami.board.vos.CommentVo;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.text.SimpleDateFormat;

@Controller(value = "com.cocoyami.board.controllers.BoardController")
@RequestMapping(value = "board")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }


    //게시글 목록
    @RequestMapping(value = "list",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getList(@RequestParam(value = "bid") String bid,
                                @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
                                @RequestParam(value = "criterion", required = false) String criterion,
                                @RequestParam(value = "keyword", required = false) String keyword) {
        // Integer 넣는 이유  : page 값이 누락되면 value 값이 null이 됨, 근데 int는 기초 타입이라서 null이 못들어감 그래서 Integer로 바꿔줌
        // defaultValue = "1" : 만약에 value 값이 null이라면 null이 아니고 기본 값인 1이 들어감
        //

        // if(page < 1){
        //  page = 1
        // } 이거를 한줄로 줄인거
        page = Math.max(1, page); // 1이랑 2중에 더 큰 값을 page에 넣어라 -> 1보다 작은 값이 들어오는걸 방지

        ModelAndView modelAndView = new ModelAndView("board/list");
        BoardEntity board = this.boardService.getBoard(bid);
        modelAndView.addObject("board", board);

        if (board != null) {
            //존재하지 않는 게시판

            int totalCount = this.boardService.getArticleCount(board,criterion,keyword);
            PagingModel paging = new PagingModel(totalCount, page);
            modelAndView.addObject("paging", paging);

            ArticleReadVo[] articles = this.boardService.getArticles(board, paging,criterion,keyword); //진짜 게시글
            modelAndView.addObject("articles", articles);

//            System.out.printf("이동 가능한 최소 페이지 : %d\n", paging.minPage);
//            System.out.printf("이동 가능한 최대 페이지 : %d\n", paging.maxPage);
//            System.out.printf("표시 시작 페이지 : %d\n", paging.startPage);
//            System.out.printf("표시 끝 페이지 : %d\n", paging.endPage);
        }
        return modelAndView;
    }


    //게시글 읽기
    @RequestMapping(value = "read",
            method = RequestMethod.GET,
            produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ModelAndView getRead(@RequestParam(value = "aid") int aid) {
        ModelAndView modelAndView = new ModelAndView("board/read");
        ArticleReadVo article = this.boardService.readArticle(aid);
        modelAndView.addObject("article", article);

        if (article != null) {
            //게시판 이름 바꾸기
            BoardEntity board = this.boardService.getBoard(article.getBoardId());
            modelAndView.addObject("board", board);
        }

        return modelAndView;
    }

    @RequestMapping(value = "read",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRead(@SessionAttribute(value = "user", required = false) UserEntity user, CommentEntity comment) {
        JSONObject responseObject = new JSONObject();
        if (user == null) {
            responseObject.put("result", CommonResult.FAILURE.name().toLowerCase());
        } else {
            comment.setUserEmail(user.getEmail());
            Enum<?> result = this.boardService.writeComment(comment);
            responseObject.put("result", result.name().toLowerCase());
        }
        return responseObject.toString();
    }


    //게시글 쓰기
    @RequestMapping(value = "write",
    method = RequestMethod.GET,
    produces = MediaType.TEXT_HTML_VALUE)
    public ModelAndView getWrite(@SessionAttribute(value = "user", required = false) UserEntity user,@RequestParam(value = "bid", required = false) String bid){
        ModelAndView modelAndView;

        if (user == null) {
            modelAndView = new ModelAndView("redirect:/member/login");
        } else {
            BoardEntity board = bid == null ? null : this.boardService.getBoard(bid);

            modelAndView = new ModelAndView("board/write");
            modelAndView.addObject("board", board);

            if (bid == null || this.boardService.getBoard(bid) == null) {
                modelAndView.addObject("result", CommonResult.FAILURE.name());
            }
            else {
                modelAndView.addObject("result", CommonResult.SUCCESS.name());
                board = bid == null ? null : this.boardService.getBoard(bid);
                modelAndView.addObject("board", board.getText());
                modelAndView.addObject("bid", board.getId());
            }
        }

        return modelAndView;
    }

    @PostMapping(value = "write",
    produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postWrite(@SessionAttribute(value = "user", required = false)UserEntity user,@RequestParam(value = "bid",required = false)String bid, ArticleEntity article){

        Enum<?> result;
        if(user == null){
            result = WriteResult.NOT_ALLOWED;
        }
        else if(bid == null){
            result = WriteResult.NO_BOARD;
        }else {
            article.setBoardId(bid);
            article.setUserEmail(user.getEmail());
            result = this.boardService.write(article);
        }
        JSONObject responseObject = new JSONObject();
        responseObject.put("result", result.name().toLowerCase());

        if (result == CommonResult.SUCCESS) { //글 작성이 성공했을 때
            System.out.println(article);
            responseObject.put("aid", article.getIndex()); // 주소창에 인덱스 번호 뜸 냠냠 굿
        }

        return responseObject.toString();
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.GET, //댓글 불러오는거
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String getComment(@SessionAttribute(value = "user", required = false) UserEntity user, @RequestParam(value = "aid") int articleIndex) {
        JSONArray responseArray = new JSONArray();
        CommentVo[] comments = this.boardService.getComments(articleIndex, user);

        for (CommentVo comment : comments) {
            JSONObject commentObject = new JSONObject();
            commentObject.put("index", comment.getIndex());
            commentObject.put("commentIndex", comment.getCommentIndex());
            commentObject.put("userEmail", comment.getUserEmail());
            commentObject.put("userNickname", comment.getUserNickname());
            commentObject.put("articleIndex", comment.getArticleIndex());
            commentObject.put("content", comment.getContent());
            commentObject.put("writtenOn", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(comment.getWrittenOn()));
            // new SimpleDataFormat("형식").format([Date 타입 객체]) : [Date 타입 객체]가 가진 일시를 원하는 형식의 문자열로 만들어 버린다.
            // commentObject.put("isSigned", user != null);
            //로그인 했는가? true 면 했다 false 면 안했다.
            // xml 에서 다 처리해줘서 != null 이거 필요없음

            commentObject.put("isSigned", comment.isSigned());
            commentObject.put("isMine", user != null && user.getEmail().equals(comment.getUserEmail()));
            //로그인을 안했을 때 user 에 널이 들어갈 수도있으니까 user!= null 넣어줌

            commentObject.put("isLiked", comment.isLiked());
            //좋아요
            commentObject.put("likeCount", comment.getLikeCount());
            //좋아요 수

            responseArray.put(commentObject);
        }

        return responseArray.toString();
    }


    @RequestMapping(value = "comment",
            method = RequestMethod.DELETE, // 댓글 삭제하는 메서드
            //위에 GET 이랑 주소를 동일하게 하고 방식만 다르게 함 -> 레스트
            //DELETE 는 의미는 없는데 삭제할 기능을 하는 메서드니까 DELETE 로 써야함-> 약속
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteComment(@SessionAttribute(value = "user", required = false) UserEntity user, CommentEntity comment) {
        //인덱스값을 받는 이유 -> 그 인덱스 값의 댓글을 삭제해야하니까 , 인덱스 값이 프라이머리 키라서 겹칠 가능성이 없음
        //formData.append('index', commentObject['index']); -> js 에서 넘어온 값

        Enum<?> result = this.boardService.deleteComment(comment, user);

        JSONObject responseObject = new JSONObject();
        //{result : failure} -> 우리가 원하는거 그래서 JSONObject 로 해줘야함
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
        //여기서 success랑 failure이 js로 감
    }

    @RequestMapping(value = "comment",
            method = RequestMethod.PATCH, //PATCH 는 수정
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchComment(CommentEntity comment, @SessionAttribute(value = "user", required = false) UserEntity user) {
        Enum<?> result = this.boardService.modifyComment(comment, user);

        JSONObject responseObject = new JSONObject();
        //String 을 받아야하면 JSON
        responseObject.put("result", result.name().toLowerCase());
        return responseObject.toString();
    }


}
