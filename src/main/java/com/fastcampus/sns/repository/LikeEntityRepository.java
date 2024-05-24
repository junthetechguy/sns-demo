package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.entity.LikeEntity;
import com.fastcampus.sns.model.entity.PostEntity;
import com.fastcampus.sns.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikeEntityRepository extends JpaRepository<LikeEntity, Integer> {
    // 원래라면 여기에도 index를 거는게 성능상 더 이득이다.
    Optional<LikeEntity> findByUserAndPost(UserEntity user, PostEntity post); // 해당 user가 해당 post에 like한게 있는지 가져오기




    // List<LikeEntity> findAllByPost(PostEntity post); => likeEntity의 모든 row를 가지고 오는 JPA method -> 아래와 같이 직접 query를 작성해주자.

    // select count(*) from "like" where post_id = (내가 부르는 값)을 위한 query가 아래와 같은데 그냥 JPA에서 제공하는 메서드를 이용하자.
    // @Query(value = "SELECT COUNT(*) FROM LikeEntity entity WHERE entity.post =:post")
    // Integer countByPost(@Param("post") PostEntity post);

    long countByPost(PostEntity post); // JPA에서 바로 제공하는 method




    @Transactional
    @Modifying
    @Query("UPDATE LikeEntity entity SET deleted_at = NOW() where entity.post = :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity);

}
