package com.fastcampus.sns.repository;

import com.fastcampus.sns.model.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Integer> { // Jpa Entity와 이 Entity의 ID를 넣어준다.
    // 이거는 Join이 어차피 이루어지지 않는 JPA Method이므로 그냥 Index를 굳이 안걸어도 된다.
    Optional<UserEntity> findByUserName(String userName); // DB 필드의 user_name과 매칭된다. 매칭이 안되면 null이 뜨므로 Optional type으로 설정해준다.
}
