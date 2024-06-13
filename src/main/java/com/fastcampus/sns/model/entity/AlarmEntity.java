package com.fastcampus.sns.model.entity;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "\"alarm\"", indexes = {
        @Index(name = "user_id_idx", columnList = "user_id")
})
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class) // opensource library에 들어있는 JsonBinaryType.class가 jsonb type이라고 정의해주자.
@SQLDelete(sql = "UPDATE \"alarm\" SET deleted_at = NOW() WHERE id=?")
@Where(clause = "deleted_at is NULL")
public class AlarmEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 알람을 받는 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    // AlarmType column을 따로 만들어야 하는 이유 2가지
    // like에 대한 alarm인지, comment에 대한 알람인지, (지금은 구현하지 않지만) comment에 달린 like인지에 대한 Type, (지금은 구현하지 않지만) 누군가 오랜만에 글을 올렸을시에 대한 Type
    // 이처럼 알람의 경우 굉장히 변화될 수 있는 환경 많다. 왜냐하면 서비스적으로 생각해봤을때 이 alarm을 보냄으로서 user의 접속을 유도할 수 있으므로
    // 굉장히 많은 종류의 알람이 생길 수 있고, 서비스 적으로도
    // 굉장히 많은 변화가 생길 수 잇으므로 언제나 AlarmType으로 enum type으로 column을 지정해서 사용하도록 하자.
    // enum을 쓰는 경우는 굉장히 변화무쌍한 Type이 생길 수 있는 경우에 enum을 쓰자.
    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;
    // 또한 alarm의 경우 alarm을 하나 받는다고 그게 바로 하나의 alarm으로 뜨질 않는다. 즉, 게시글 하나를 올렸는데 100명이 like한 경우
    // 100명 각각 like를 눌렀다고 개별 alarm을 보내는게 아니라 A 외 99명이 like했습니다라고 알람창이 떠야하기 때문에
    // alarm을 뭉쳐서 보여주기 위해선 이렇게 AlarmType을 따로 별도의 column으로 구분하도록 하자.


    // 지금은 당장 "new comment"와 "new like"가 alarm text의 전부이지만
    // 실전 서비스에서는 그런 간단한 text가 아니라, 누가 comment를 달았는가, 누가 like를 달았는가를 보여주고,
    // 알람을 눌렀을 경우에 해당 post로 이동하게 된다.
    // 따라서 이 alarm을 누가 발생시켰는가에 대한 정보와 이 alarm이 어떤 post에 발생이 되었는지에 대한 주체(post, 댓글, 스토리 등)가 필요하다.
    // 그에 대한 정보를 args에 저장을 해두자. 지금은 사용하지 않지만 서비스가 확장할 경우를 대비하는 경우를 위해서 미리 해두자.
    // 지금까지는 column type으로 postgres에서 지원하는 type(Timestamp, String, Integer 등)만 지정해왔는데
    // 이렇게 데이터 클래스를 넣으려면 postgres에서 json type이라는 column을 지원을 하므로(MySQL에서는 8점대부터 Json 타입이 추가됨)
    // json으로 column을 넣어서 변화무쌍하고 유연하게 AlarmArgs를 받을 수 있게 해주자.
    // 가령, 00씨가 새 코멘트를 작성했습니다. 라는 알람은 postId와 commentId 모두 다 가지고 있어야 함.
    // 00외 2명이 새 코멘트를 작성했습니다. 라는 알람은 commentId가 3개가 들어가야함.
    // 이런 경우에서는 alarm type에 따라 field가 하나씩 추가되어야 하는데
    // 뭐 이런식으로 field가 고정되어 있지 않고 계속 많아지게 될 경우 이 모든 field를 column으로 만들게 되면 alarm이 어떤 type인지에 따라서
    // null인 필드가 발생할 수 밖에 없는데 그걸 null로 넣게 되면 데이터(디스크 블록)를 낭비하는 셈이 되므로 비효율적이게 된다.
    // 따라서 이 경우에는 이런 식으로 json으로 argument를 설정해서 유연하게 대처하자.
    @Type(type = "jsonb") // postgres는 column으로 json type(json data 그대로 DB에 저장)과 jsonb type(json binary; json data를 한번 압축해서 DB에 저장)이 있다. 또한 jsonb type의 경우에만 index를 걸 수 있다(index는 걸수만 있으면 무조건 성능상으로 좋으므로 무조건 걸어야만 한다)
    @Column(columnDefinition = "json") // 근데 jsonb type은 postgres에만 있는 type이므로 jpa에는 존재하지 않다. 따라서 내가 사용하는 jpa/hibernate는 jsonb type을 지원하지 않으므로 jsonb type을 entity의 속성으로 지정하기 위해 사용하는 opensource library를 추가해주자.
    private AlarmArgs args;

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

    public static AlarmEntity of(UserEntity userEntity, AlarmType alarmType, AlarmArgs args) {
        AlarmEntity entity = new AlarmEntity();
        entity.setUser(userEntity);
        entity.setAlarmType(alarmType);
        entity.setArgs(args);
        return entity;
    }
}
