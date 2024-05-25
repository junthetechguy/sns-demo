package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.entity.AlarmEntity;
import com.fastcampus.sns.model.entity.LikeEntity;
import com.fastcampus.sns.model.entity.PostEntity;
import com.fastcampus.sns.model.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlarmEntityRepository extends JpaRepository<AlarmEntity, Integer> {
    // 아래와 같이 userEntity로 alarm을 찾을때 실제로는 alarm table에서
    // 즉, AlarmEntity에서 user column이 실제로는 ManyToOne으로 JoinColumn이 user_id로 되어 잇으므로 이 말인 즉슨 실제 이 alarm table에는 user_id로 들어가 있어서
    // 사실 이 userEntity로 alarm을 찾을때는 그냥 user_id로 한번에 줘서 찾도록 하면 service 단에서 해당 userEntity가 있는지 없는지 검사하는 코드가 사라지게 된다.
    // 차라리 굳이 그럴 필요없이 JwtTokenFilter 부분에서 이미 SetAuthentication으로 authentication으로 넣어줄때부터 이미 pricipal로 user를 넣어주게 되므로
    // 그냥 User user = (User).authentication.getPricipal()의 느낌으로 여기서 User를 뽑아낸 다음에 이 User의 user_id를 service 단으로 내려보내자

    // Page<AlarmEntity> findAllByUser(UserEntity user, Pageable pageable);

    // 그렇기 때문에 user_id로 한번에 찾아주자.
    Page<AlarmEntity> findAllByUserId(Integer userId, Pageable pageable);
}
