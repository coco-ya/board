package com.cocoyami.board.mappers;

import com.cocoyami.board.entities.board.ArticleEntity;
import com.cocoyami.board.entities.board.BoardEntity;
import com.cocoyami.board.entities.board.CommentEntity;
import com.cocoyami.board.vos.ArticleReadVo;
import com.cocoyami.board.vos.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface IBoardMapper {
    BoardEntity selectBoardId(@Param(value = "id") String id);

    //인터센터에 BoardEntity 넘겨주기 위함
    BoardEntity[] selectBoards();

    int insertArticle(ArticleEntity article);

    ArticleReadVo selectArticleByIndex(@Param(value = "index") int index);


    int updateArticle(ArticleEntity article);

    ArticleReadVo[] selectArticlesByBoardId(@Param(value = "boardId") String boardId,
                                            @Param(value = "criterion") String criterion,
                                            @Param(value = "keyword") String keyword,
                                            @Param(value = "limit") int limit,
                                            @Param(value = "offset") int offset);

    //게시글 페이징
    int selectArticleCountByBoardId(@Param(value = "boardId") String boardId,
                                    @Param(value = "criterion") String criterion,
                                    @Param(value = "keyword") String keyword);

    int insertComment(CommentEntity comment);

    CommentVo[] selectCommentsByArticleIndex(@Param(value = "articleIndex") int articleIndex,
                                             @Param(value = "userEmail") String userEmail);

    CommentEntity selectCommentByIndex(@Param(value = "index") int index);

    int updateComment(CommentEntity comment); //들어온 갯수 세줄라고 -> 성공갯수

    int deleteCommentByIndex(@Param(value = "index") int index);
}
