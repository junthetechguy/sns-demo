package com.fastcampus.sns.service;

import com.fastcampus.sns.exception.ErrorCode;
import com.fastcampus.sns.exception.SnsApplicationException;
import com.fastcampus.sns.model.AlarmArgs;
import com.fastcampus.sns.model.Comment;
import com.fastcampus.sns.model.Post;
import com.fastcampus.sns.model.entity.*;
import com.fastcampus.sns.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class PostService {
    private final PostEntityRepository postEntityRepository;
    private final UserEntityRepository userEntityRepository;
    private final LikeEntityRepository likeEntityRepository;
    private final CommentEntityRepository commentEntityRepository;
    private final AlarmEntityRepository alarmEntityRepository;
    private final AlarmService alarmService;

    @Transactional
    public void create(String title, String body, String userName) {
        UserEntity userEntity = getUserEntityOrException(userName);
        postEntityRepository.save(PostEntity.of(title, body, userEntity));
    }

    @Transactional
    public Post modify(String title, String body, String userName, Integer postId) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);
        // 지금 접속된 유저와 글을 작성한 유저가 일치하는지 확인
        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }

        postEntity.setTitle(title);
        postEntity.setBody(body);

        return Post.fromEntity(postEntityRepository.saveAndFlush(postEntity)); // DB의 updated_at 필드에 정확한 시각을 반영하기 위해서 JPA의 save()가 아니라 saveAndFlush()를 사용해서 Buffer를 flush하는 시점을 늦춰서 updated_at에 null이 아니라 제대로 시각이 들어가도록 하자.
    }

    @Transactional
    public void delete(String userName, Integer postId) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        if (postEntity.getUser() != userEntity) {
            throw new SnsApplicationException(ErrorCode.INVALID_PERMISSION, String.format("%s has no permission with %s", userName, postId));
        }
        likeEntityRepository.deleteAllByPost(postEntity);
        commentEntityRepository.deleteAllByPost(postEntity);
        postEntityRepository.delete(postEntity);
    }

    public Page<Post> list(Pageable pageable) {
        return postEntityRepository.findAll(pageable).map(Post::fromEntity); // 이렇게 findAll()을 하게 되면 PostEntity의 형태로 반환이 되는데 DAO는 서비스 단에서 사용하면 안되므로 이것을 Post 형태로 mapping 시켜서 사용하도록 하자.
    }

    public Page<Post> my(String userName, Pageable pageable) {
        UserEntity userEntity = getUserEntityOrException(userName);

        return postEntityRepository.findAllByUser(userEntity, pageable).map(Post::fromEntity);
    }
    @Transactional
    public void like(Integer postId, String userName) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        // check liked(이미 like한 사람이면) -> throw
        likeEntityRepository.findByUserAndPost(userEntity, postEntity).ifPresent(it -> {
            throw new SnsApplicationException(ErrorCode.ALREADY_LIKED, String.format("userName %s already liked post %s", userName, postId));
        });

        // like save
        likeEntityRepository.save(LikeEntity.of(userEntity, postEntity));

        // alarm save
        AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_LIKE_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
        alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());

    }

    @Transactional
    public long likeCount(Integer postId) { // null일 필요가 없으므로 Integer가 아니라 int로 반환해도 된다(근데 지금은 그냥 범위가 더 큰 long으로 가져온다)
        // 만약에 null일 수도 있다면 Integer처럼 class로 wrapping을 한번 해줘야 한다.

        // post가 존재할때만 like를 counting할 수 있으므로 먼저 post 존재 여부를 검사해준다.
        PostEntity postEntity = getPostEntityOrException(postId);

        /* 아래와 같은 식으로 가져와서 count를 return하는것은 일단 likeEntity의 DB 상에서의 모든 row를 DB에서 싹 가지고 오기 때문에
        지금 필요한 것은 갯수 뿐이므로 그냥 바로 postgres에서 count를 바로 가지고 오는 query를 이용하자.
        즉, findAllByPost를 하면 모든 row를 다 가지고 와서 거기서 matching을 시작하므로 그냥 바로 갯수만 가지고 오는 light한 query를
        날려서 가지고 오자.
        List<LikeEntity> likeEntities = likeEntityRepository.findAllByPost(postEntity);
        return likeEntities.size();
        */

        return likeEntityRepository.countByPost(postEntity);
        // 이러한 방법이 전체를 다 가지고 온 다음에 size를 계산하는 방식보다 훨씬 더 최적화(더 가벼운 query) 된 방식이다.
    }

    @Transactional
    public void comment(Integer postId, String userName, String comment) {
        UserEntity userEntity = getUserEntityOrException(userName);
        PostEntity postEntity = getPostEntityOrException(postId);

        // comment save
        commentEntityRepository.save(CommentEntity.of(userEntity, postEntity, comment));
        // alarm save
        AlarmEntity alarmEntity = alarmEntityRepository.save(AlarmEntity.of(postEntity.getUser(), AlarmType.NEW_COMMENT_ON_POST, new AlarmArgs(userEntity.getId(), postEntity.getId())));
        alarmService.send(alarmEntity.getId(), postEntity.getUser().getId());
    }

    public Page<Comment> getComments(Integer postId, Pageable pageable) {
        PostEntity postEntity = getPostEntityOrException(postId);
        return commentEntityRepository.findAllByPost(postEntity, pageable).map(Comment::fromEntity);
    }


    // 자주 쓰이는 코드들(공통 logic 코드)은 아래와 같이 따로 private 메소드로 빼줘서 깔끔하게 만들어주자.
    // post exist check
    private UserEntity getUserEntityOrException(String userName) {
        return userEntityRepository.findByUserName(userName).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.USER_NOT_FOUND, String.format("%s not found", userName)));

    }
    // user exist check
    private PostEntity getPostEntityOrException(Integer postId) {
        return postEntityRepository.findById(postId).orElseThrow(() ->
                new SnsApplicationException(ErrorCode.POST_NOT_FOUND, String.format("%s not found", postId)));

    }
}
