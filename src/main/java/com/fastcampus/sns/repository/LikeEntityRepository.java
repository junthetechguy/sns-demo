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

    // select count(*) from "like" where post_id = (내가 부르는 값)을 위한 native query가 아래와 같은데 그냥 JPA에서 제공하는 메서드를 이용하자.
    // @Query(value = "SELECT COUNT(*) FROM LikeEntity entity WHERE entity.post =:post")
    // Integer countByPost(@Param("post") PostEntity post);

    long countByPost(PostEntity post); // JPA에서 바로 제공하는 method


    // jpa는 persistence(영속성)이란걸 관리(persistence context)를 하므로 데이터를 DB 테이블에서 이 데이터를 가지고 왔을때 이 data의 lifecycle를 관리하는 것을
    // 이 application 안에서 하게된다. 즉, 이 데이터가 변경이 되거나 삭제가 되거나 하는 변화를 이 persistence 관리(persistence context)를 통해서 jpa가 먼저 체크를 한 후에
    // 실제 transaction이 끝나고 commit이 일어났을때 이 데이터의 이러한 상태를 DB Table에 업데이트를 하게 된다.
    // 따라서 이러한 persistence 관리(persistence context)를 위해선 일단 data를 DB Table에서 가지고 와야 하므로 아래의 deleteAll 같은 경우에는
    // 내가 원하는 동작은 데이터를 DB Table에서 가지고 와서 그걸 비교해가지고 삭제시키는 로직이 아니라 그냥 삭제가 되어야 하는 데이터이므로
    // delete query만 날리면 된다. 근데 아래의 deleteAll은 일단 삭제할 데이터를 다 DB Table에서부터 가지고 와서 삭제를 하므로 비효율적이라 굳이 데이터를 가지고 오지 말고
    // 따라서 jpa로 delete하는 경우에는 반드시 jpa에서 제공하는 delete를 사용하지 말고 native query를 바로 날려보내자.
    // void deleteAllByPost(PostEntity post); delete 같은 것은 JPA에서 제공하는 것을 쓰지 말고 위와 같이 직접 query를 작성해서 쓰는 것이 훨씬 효율적으로 서버를 만들 수 있다

    @Transactional
    @Modifying // update 관련 query이므로 Modifying annotation을 달아줘야 정상 동작을 한다.
    @Query("UPDATE LikeEntity entity SET deleted_at = NOW() where entity.post = :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity);

}
