package com.fastcampus.sns.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

// 항상 Entity는 @Setter, @Getter, @Entity, @NoArgsConstructor가 필수다.
@Setter
@Getter
@Entity // Jpa Repository에서 사용될 entity임을 나타내는 Annotation
@Table(name = "\"user\"") // postgreSQL 같은 경우에는 이미 user라는 table이 존재하므로 (실제로 테이블에 접근할 수 있는 user의 권한을 관리하는 테이블) 항상 ”\”를 user에 붙여야 내가 만든 user 테이블로 인식된다.
@SQLDelete(sql = "UPDATE \"user\" SET deleted_at = NOW() WHERE id=?") // Delete의 경우 삭제된 시간을 넣어줄때 만약 delete sql이 날라오게 되면 이런식으로 deleted_at에 자동으로 시간이 들어가게 하자.
@Where(clause = "deleted_at is NULL") // Select를 할때는 삭제가 안된 애들(deleted_at이 NULL인 애들)만 가지고 올 수 있게 하자.
@NoArgsConstructor
/*
나는 jpa를 사용하기 때문에
클래스 자체의 변화에 굉장히 민감하기때문에 단순히 DTO에 있는 어떤 필드를 변경하고 싶었고
db의 변경에는 영향을 주고 싶지 않을때가 존재하므로 언제나 entity와 DTO로 분리해서 사용하자.
 */
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB에서 Entity의 ID가 PostgreSQL에서 사용하는 Sequence로 Automatic Increase 됨
    private Integer id;

    @Column(name = "user_name") // DB 필드는 user_name으로 저장이 되지만 실제로 JPA에서의 Entity에서는 필드명이 userName으로 들어간다.
    private String userName;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    @Enumerated(EnumType.STRING) // Enum class의 경우 항상 이런식으로 이 Enum class를 쓰는 곳에서 그 Type을 정의해서 사용한다.
    private UserRole role = UserRole.USER; // 이 프로젝트에서는 default로 무조건 role에는 USER라고 들어가게 된다.

    @Column(name = "register_at")
    private Timestamp registeredAt;

    /*
    * UserEntity를 만들때는 항상 registered_at, updated_at, deleted_at이라는 필드를 함께 넣도록 하자.
    * 그래야 나중에 문의가 들어왔을때 디버깅을 해야하는데 해당 데이터가
    * 언제 저장이 되고 업데이트가 되고 삭제가 되었는지에 대한 정보를 가지고 있어야 쉬우므로 그런것이다.
    * 따라서 디비에 저장할때는 어떤 테이블이든지 이 3개의 at을 함께 넣어주도록 하여 데이터 관리를 더 편하게 진행되도록 하자.
    * 또한 해당 db의 데이터를 삭제하는 경우에는 해당 row를 실제로 삭제하는게 아니라 deleted_at 부분만 저장을 해놓는 soft delete를 하도록 하여
    * 삭제된 시각이나 플래그를 줘서 절대로 hard delete를 하지 말자.
    * 그래야 나중에 CS(Customer Support)가 들어왔을때 대처가 쉽다.
    */

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    @PrePersist // DB에 Persist하게 Entity가 Create되기 전에 자동으로 시각을 자동으로 넣어준다.
    void registeredAt() {
        this.registeredAt = Timestamp.from(Instant.now());
    }

    @PreUpdate // DB에 있는 Entity의 필드를 Update할 경우 Update하기 전에 그 수정 시각을 자동으로 넣어준다.
    void updatedAt() {
        this.updatedAt = Timestamp.from(Instant.now());
    }

    // User table에 UserEntity를 CRUD할때 DAO를 그대로 갖다가 넣는게 아니라 DAO를 최대한 분리하기 위해서 UserEntity를 반환하는 of 메소드
    public static UserEntity of(String userName, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUserName(userName);
        userEntity.setPassword(password);
        return userEntity;
    }
    /*
    DB에 저장할때만 Entity를 사용하고,
    서비스단에서는 사용할때는 DTO로 분리해서 사용하여 최대한 DB 변화에 영향을 주면 안되도록 하자.
     */
}
