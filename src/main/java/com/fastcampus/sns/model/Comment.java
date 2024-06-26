package com.fastcampus.sns.model;

import com.fastcampus.sns.model.entity.CommentEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;

@Getter
@AllArgsConstructor
public class Comment {
    private Integer id;

    private String comment;

    private String userName; // comment에서 필요한 user 정보는 userName만 필요하므로 Entity에서 사용하는 UserEnity type이 아니라 DTO에서는 따로 String userName으로 설정해두자.

    private Integer postId; // 마찬가지로 comment에서 필요한 post 정보는 postId만 필요하므로 Entity에서 사용하는 PostEntity type이 아니라 DTO에서는 따로 Integer postId로 설정해두자.

    private Timestamp registeredAt;

    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public static Comment fromEntity(CommentEntity entity) {
        return new Comment(
                entity.getId(),
                entity.getComment(),
                entity.getUser().getUserName(),
                entity.getPost().getId(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }
}
