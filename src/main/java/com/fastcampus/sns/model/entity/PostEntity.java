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
@Entity
@Table(name = "\"post\"")
@SQLDelete(sql = "UPDATE \"post\" SET deleted_at = NOW() WHERE id=?")
@Where(clause = "deleted_at is NULL")
public class PostEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "body", columnDefinition = "TEXT") // body 부분을 default로 하게 되면 String으로 선택이 되게 되는데 그러면 사이즈가 작게 설정이 되므로 TEXT Type(String에 비해 사이즈가 더 커서 더 많은 글 저장 가능)으로 설정해주자.
    private String body;

    @ManyToOne // 이 post를 작성한 사람(user)이 누군지를 mapping을(하나의 user가 여러개의 post를 쓸 수 있으므로 ManyToOne으로 설정) 시켜서 post를 가지고 올때 이 userEntity도 같이 가지고 올 수 있게 user table과의 join을 시켜둔다.
    @JoinColumn(name = "user_id")
    private UserEntity user;

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

    public static PostEntity of(String title, String body, UserEntity userEntity) {
        PostEntity entity = new PostEntity();
        entity.setTitle(title);
        entity.setBody(body);
        entity.setUser(userEntity);
        return entity;
    }
}
