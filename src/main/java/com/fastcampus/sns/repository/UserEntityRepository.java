package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> { // Jpa Entity와 이 Entity의 ID를 넣어준다.
    // 원래라면 여기에도 index를 거는게 성능상 더 이득이다.
    Optional<UserEntity> findByUserName(String userName); // DB 필드의 user_name과 매칭된다. 매칭이 안되면 null이 뜨므로 Optional type으로 설정해준다.
}
