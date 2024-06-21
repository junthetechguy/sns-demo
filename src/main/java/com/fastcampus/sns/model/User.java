package com.fastcampus.sns.model;

import com.fastcampus.sns.model.entity.UserEntity;
import com.fastcampus.sns.model.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

// @Getter // @Getter만 있으면 Redis에 저장할때 toString()이 없어서 이 instance의 hashcode 값이 찍히므로 @Data로 달아주자.
@Data
@NoArgsConstructor // default constructor : Redis에 객체를 만들때는 먼저 NoArgsConstructor를 이용해서 생성 후 data를 채우므로 @NoArgsConstructor를 달아주자.
@AllArgsConstructor // 모든 인자 constructor
@JsonIgnoreProperties(ignoreUnknown = true) // JsonIgnore를 먹히게 하기 위한 Annotation
public class User implements UserDetails { // Token으로 User를 가지고 올때 UserDetails를 implement해서 override로 가지고 오지 않으면 제대로 user를 가져오지 못하므로 구현해주자.
    private Integer id;
    private String username; // UserDetails에 getUsername() method(Authentication에서 principal의 이름을 가냥 바로 가져오는 부분을 implement 해야하므로 그냥 annotation을 @Data 단걸로 퉁치도록 하자.
    private String password;
    private UserRole userRole;
    private Timestamp registeredAt;
    private Timestamp updatedAt;
    private Timestamp deletedAt;


    public static User fromEntity(UserEntity entity) { // Entity를 DTO로 변환해주는 메소드
        return new User(
                entity.getId(),
                entity.getUserName(),
                entity.getPassword(),
                entity.getRole(),
                entity.getRegisteredAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt()
        );
    }

    @Override // toString()을 Override해서 구현함으로서 Redis에 저장될때 instance의 hashcode 값이 아니라 username이 찍히도록 해주자.
    public String toString() {
        return username;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.toString()));
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return deletedAt == null;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return deletedAt == null;
    }

}
