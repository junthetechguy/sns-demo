package com.fastcampus.sns.model.entity;

import com.fastcampus.sns.model.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Setter
@Getter
@Entity // Jpa Repository에서 사용될 entity임을 나타내는 Annotation
@Table(name = "\"user\"") // postgreSQL 같은 경우에는 이미 user라는 table이 존재하므로 (실제로 테이블에 접근할 수 있는 user의 권한을 관리하는 테이블) 항상 ”\”를 user에 붙여야 내가 만든 user 테이블로 인식된다.
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = NOW() WHERE id=?")
@Where(clause = "deleted_at is NULL")
@NoArgsConstructor

/*
model의 entity는 DAO로 실제로 DB의 Table에서 부터 Jpa Repository(@Repository 붙은 것들)을 통해서 실제로 mapping되는 클래스들의 집합이고,
model에 속하지만 entity가 아닌 것들은 DTO로 DAO에서 정보를 가지고 와서 서비스 단에서 뭔가를 처리할 때 사용한다.
왜냐하면 이 2개가 구분이 안되면 나는 jpa를 사용하기 때문에
이 클래스 자체의 변화에 굉장히 민감하기때문에 단순히 DTO에 있는 어떤 필드를 변경하고 싶었고
db의 변경에는 영향을 주고 싶지 않을때가 존재하므로 언제나 DAO와 DTO로 분리해서 사용하자.
 */
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 Entity의 ID가 자동으로 Increase 됨
    private Integer id;

    @Column(name = "user_name") // DB 필드는 user_name으로 저장이 되지만 실제로 JPA에서의 Entity에서는 필드명이 userName으로 들어간다.
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    @Column(name = "register_at")
    private Timestamp registeredAt;

    /*
    * UserEntity를 만들때는 항상 registered_at, updated_at, deleted_at이라는 필드를 함께 넣도록 하자.
    * 그래야 나중에 문의가 들어왔을때 디버깅을 해야하는데 해당 데이터가
    * 언제 저장이 되고 업데이트가 되고 삭제가 되었는지에 대한 정보를 가지고 있어야 쉬우므로 그런것이다.
    * 따라서 디비에 저장할때는 어떤 테이블이든지 이 3개의 at을 함께 넣어주도록 하여 데이터 관리를 더 편하게 진행되도록 하자.
    * 또한 해당 db의 데이터를 삭제하는 경우에는 해당 row를 실제로 삭제하는게 아니라 deleted_at 부분만 저장을 해놓는 soft delete를 하도록 하여 삭제된 시각이나 플래그를 줘서 절대로 hard delete를 하지 말자. 그래야 나중에 CS(Customer Support)가 들어왔을때 대처가 쉽다.
    */

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist
    void registeredAt() {
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    public static UserEntity of(String userName, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);
        return userEntity;
    }

}
