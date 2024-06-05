package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.entity.CommentEntity;
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
public interface CommentEntityRepository extends JpaRepository<CommentEntity, Integer> {
    // findById 처럼 애초에 PK인 Id로 가져올때는 이미 PK 자체는 항상 Index가 걸려있어서 Index를 타서 가져오므로 성능상
    // 문제가 되지 않는다. 하지만 PostEntity를 통해서 DB에서 가지고 오는데 이땐 인덱스가 안걸려 있으므로
    // 전체를 조인을 일단 진행한 후에 Post ID로 검색하므로 속도가 굉장히 느려지게 되므로 성능상 이슈가 생긴다
    // 따라서 애초에 DB 테이블을 만들때부터(지금은 ddl-auto이므로 CommentEntity에다가 직접 설정해주자. Index를 Post ID로 한다고 설정해주면 Join이 일어나지 않으므로 더 빨라진다.
    Page<CommentEntity> findAllByPost(PostEntity post, Pageable pageable);

    // jpa는 persistency(영속성)이란걸 관리를 하므로 데이터를 DB 테이블에서 이 데이터를 가지고 왔을때 이 data의 lifecycle를 관리하는 것을
    // 이 application 안에서 하게된다. 즉, 이 데이터가 변경이 되거나 삭제가 되거나 하는 변화를 이 persistency 관리를 통해서 jpa가 먼저 체크를 한 후에
    // 실제 transaction이 끝나고 commit이 일어났을때 이 데이터의 이러한 상태를 DB Table에 업데이트를 하게 된다.
    // 따라서 이러한 persistency 관리를 위해선 일단 data를 DB Table에서 가지고 와야 하므로 아래의 deleteAll 같은 경우에는
    // 내가 원하는 동작은 데이터를 DB Table에서 가지고 와서 그걸 비교해가지고 삭제시키는 로직이 아니라 그냥 삭제가 되어야 하는 데이터이므로
    // delete query만 날리면 된다. 근데 아래의 deleteAll은 일단 삭제할 데이터를 다 DB Table에서부터 가지고 와서 삭제를 하므로 비효율적이라 굳이 데이터를 가지고 오지 말고
    // 따라서 jpa로 delete하는 경우에는 반드시 jpa에서 제공하는 delete를 사용하지 말고 native query를 바로 날려보내자.
    // void deleteAllByPost(PostEntity post); delete 같은 것은 JPA에서 제공하는 것을 쓰지 말고 위와 같이 직접 query를 작성해서 쓰는 것이 훨씬 효율적으로 서버를 만들 수 있다


    @Transactional
    @Modifying
    @Query("UPDATE CommentEntity entity SET deleted_at = NOW() where entity.post = :post")
    void deleteAllByPost(@Param("post") PostEntity postEntity);

}
