package com.fastcampus.sns.model.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "\"like\"")
@SQLDelete(sql = "UPDATE \"like\" SET deleted_at = NOW() WHERE id=?")
@Where(clause = "deleted_at is NULL")
public class LikeEntity {
    // like를 한번 누를때마다 like table에 row가 하나씩 생기는 형태로 개발해보자.
    // 왜냐하면 지금의 sns app에서는 구현이 안되겠지만, 이 사람의 post별 like가 눌린 순서라든지, like를 취소했거나 승인했다가 하는 동작을 위해서
    // like 1개당 row를 1개씩 차지하도록 개발한다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private PostEntity post; // 어떤 post에 대해서 like를 눌렀는지 정보 저장

    @Column(name = "register_at")
    private Timestamp registeredAt;

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

    public static LikeEntity of(UserEntity userEntity, PostEntity postEntity) {
        LikeEntity entity = new LikeEntity();
        entity.setUser(userEntity);
        entity.setPost(postEntity);
        return entity;
    }
}
