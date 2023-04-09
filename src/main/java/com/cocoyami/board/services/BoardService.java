package com.cocoyami.board.services;

import com.cocoyami.board.entities.board.ArticleEntity;
import com.cocoyami.board.entities.board.BoardEntity;
import com.cocoyami.board.entities.board.CommentEntity;
import com.cocoyami.board.entities.member.UserEntity;
import com.cocoyami.board.enums.CommonResult;
import com.cocoyami.board.enums.board.CommentResult;
import com.cocoyami.board.enums.board.WriteResult;
import com.cocoyami.board.interfaces.IResult;
import com.cocoyami.board.mappers.IBoardMapper;
import com.cocoyami.board.models.PagingModel;
import com.cocoyami.board.vos.ArticleReadVo;
import com.cocoyami.board.vos.CommentVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service(value = "com.cocoyami.board.services.BoardService")
public class BoardService {

    private final IBoardMapper boardMapper;

    public BoardService(IBoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public BoardEntity getBoard(String id) {
        return this.boardMapper.selectBoardId(id);
    }

    public BoardEntity[] getBoards() {
        return this.boardMapper.selectBoards();
    }

    public Enum<? extends IResult> write(ArticleEntity article) {
        BoardEntity board = this.boardMapper.selectBoardId(article.getBoardId());
        if (board == null) {
            return WriteResult.NO_BOARD;
        }
        return this.boardMapper.insertArticle(article) > 0 ?
                CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    //게시글 불러오기
    public ArticleReadVo readArticle(int index) {

        ArticleReadVo article = this.boardMapper.selectArticleByIndex(index);
        if (article != null) {
            article.setView(article.getView() + 1); //게시글 들어갈 때 마다 +1 해주고
            this.boardMapper.updateArticle(article); //update 로 보내서 조회수 수정
        }
        return article;
    }

    //게시글 목록 가져오기 + 한페이지당 10개씩만
    public ArticleReadVo[] getArticles(BoardEntity board, PagingModel paging, String criterion, String keyword) {

//        if (criterion == null || keyword == null) {
        return this.boardMapper.selectArticlesByBoardId(
                board.getId(),
                criterion,
                keyword,
                paging.countPerPage, // limit : 한페이지 당 표시할 갯수(10개)
                (paging.requestPage - 1) * paging.countPerPage);// offset : 생략할 게시글 갯수
//        }
    }

    //게시글 검색
    public int getArticleCount(BoardEntity board, String criterion, String keyword) {
        //        if (criterion == null || keyword == null) {
        return this.boardMapper.selectArticleCountByBoardId( //전체 게시물 개수 돌려줌
                board.getId(),
                criterion,
                keyword);
    }

    public Enum<? extends IResult> writeComment(CommentEntity comment) {
        ArticleEntity article = this.boardMapper.selectArticleByIndex(comment.getArticleIndex());
        if (article == null) {
            return CommonResult.FAILURE;
        }

        return this.boardMapper.insertComment(comment) > 0 ?
                CommonResult.SUCCESS :
                CommonResult.FAILURE;
    }

    public CommentVo[] getComments(int articleIndex, UserEntity signedUser) {
        return this.boardMapper.selectCommentsByArticleIndex(articleIndex,
                signedUser == null ? null : signedUser.getEmail());
    }

    //댓글 삭제
    //실페 : deleteComment 의 호출결과가 0이다-> 영향을 받은 게 없다 ->삭제 안됨
    //로그인 안되있고 + 삭제하려는 댓글이 니 댓글이 아닌 경우, 니 : 세션,
    //댓글이 존재하지않음
    public Enum<? extends IResult> deleteComment(CommentEntity comment, UserEntity user) {
        //comment 에는 index 값이 들어있음

        CommentEntity existingComment = this.boardMapper.selectCommentByIndex(comment.getIndex());
        if (existingComment == null) {
            //니가 지금 삭제하려는 댓글이 존재하지 않는다
            return CommentResult.NO_SUCH_COMMENT;
        }
        //여기까지 왔다는거는 댓글이 존재는 한다.
        //로그인한 사람의 댓글인지 확인(비교)
        //이게 보안조치 느낌, 이거 없으면 내 댓글 아니어도 브라우저에서 삭제할 수 있음
        if (user == null || !user.getEmail().equals(existingComment.getUserEmail())) {
            //앞에 조건이 참이면 뒤에 조건은 안쳐다봐도됨 -> ||
            return CommentResult.NOT_ALLOWED;
        }
        return this.boardMapper.deleteCommentByIndex(comment.getIndex()) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

    //댓글 수정
    @Transactional
    public Enum<? extends IResult> modifyComment(CommentEntity comment, UserEntity user) {
        //comment는 index랑 content밖에 안가지고 있음
        CommentEntity existingComment = this.boardMapper.selectCommentByIndex(comment.getIndex());
        //existingComment 얘가 값 다 가지고 있음
        if (existingComment == null) {
            return CommentResult.NO_SUCH_COMMENT;
        }
        if (user == null || !user.getEmail().equals(existingComment.getUserEmail())) {
            return CommentResult.NOT_ALLOWED;
        }
        Date createdOn = new Date();
        existingComment.setContent(comment.getContent());
        existingComment.setWrittenOn(createdOn);

        return this.boardMapper.updateComment(existingComment) > 0
                ? CommonResult.SUCCESS
                : CommonResult.FAILURE;
    }

}
