package com.fastcampus.sns.configuration;

import com.fastcampus.sns.model.User;
import io.lettuce.core.RedisURI;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

// DB 같은 경우에는(나의 경우 Postgres) 붙일때 application.yaml에서 spring.datasource에 Postgres URL이나 username이나 pw를 설정해두면 Spring Boot Application에서
// 자동으로 property를 만들어주고, 이 DB에도 접근할 수 있는 connection도 해주고, 그냥 나는 repository package에서 repository JPA interface로 간단하게 메소드만 작성하면 되는데
// Redis도 비슷한 방식으로 진행한다. 다만 Connection과 RedisTemplate(Redis Command를 내가 사용하는 프로그래밍 언어 코드로 작성할 수 있게 해주는 helper 클래스)도 내가 직접 만들어서 Bean으로 띄워서 그 Bean을 가져다가 쓰는 방식으로 사용해야 한다.

@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfiguration {

    // Redis 뿐만 아니라 DB나 다른 것들도 property 설정 관련 정보는 싹다 application.yaml 파일에 넣고, 그 데이터를 소스코드로 가지고 와서 사용하게 되는데
    // 그 이유는 최대한 decoupling해서 재사용성이나 환경 migration시 발생할 수 있는 문제점을 따로 value 형태로 applicaiton.yaml만 고치면 직접 소스코드를 수정하지 않아도
    // 되도록 하기 위함이다. 또한 지금의 경우에는 이러한 yaml file을 resources package 아래에 뒀지만 따로 서버를 둬서 그 서버에서 끌어와서 이 yaml file을 update하는 경우도 있다.
    // 그러면 yaml file은 단지 configuration 역할만 할뿐이고, business logic이 아니기 때문에 소스 코드 수정 없이 그냥 configuration만 이쪽으로 설정해두면 개꿀이다.
    // 그렇기 때문에 언제나 yaml file로 빼두자.
    // 지금의 redis의 경우에는 spring.redis.url만 yaml file에 설정해뒀는데 그러면 자동으로 RedisProperties를 Bean으로 만들어준다. 그러면 이것을 그대로 private final로 가져다 쓰자,
    private final RedisProperties redisProperties;

    // 실제 Redis 서버 정보를 가지고 있는 놈으로 이 RedisConnectionFactory가 있어야만 실제 어떤 Redis에 연결할지 결정할 수 있음
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        RedisURI redisURI = RedisURI.create(redisProperties.getUrl());
        // 해당 Redis 서버의 URI를 그대로 가져와서 내 application과 연결을 factory를 통해서 해야하는데 실제 Redis와 내 Application을 연결해주는 Factory(library)는 2개가 있다.
        // 1. Zedis(옛날 legacy 방식) library 2. Lettuce(Letty를 사용하므로 성능이 좋음) library
        org.springframework.data.redis.connection.RedisConfiguration configuration = LettuceConnectionFactory.createRedisConfiguration(redisURI);
        LettuceConnectionFactory factory = new LettuceConnectionFactory(configuration);
        factory.afterPropertiesSet(); // Lettuce로 만든 factory 초기화(initializing) 시키는 용도
        return factory;
    }

    // Caching할때 생각해야해봐야 하는건 첫번째로 데이터의 변경이 너무 많은 데이터는 캐싱을 해봤자 소용이 없다.
    // 캐싱은 DB에 있는 데이터를 접근의 코스드가 조금 더 적은 곳에 놔둬서 가지고 오기 때문에 만약 원본 데이터인 DB의 데이터가 너무 많이 변화하게 되면 캐싱도 당연히 변화를 해야 하므로
    // 이 경우에는 caching을 eviction 시키고 DB에 저장된 데이터를 다시 가지고 와서 Caching에 다시 올려 놓는 상태로 동작을 하게 되는데
    // 너무 데이터가 자주 변하면 이 동작이 존나게 많이 발생하므로 select(=DBIO) 또한 존나게 발생하므로 그러면 안되므로 원본 데이터인 DB의 데이터가 변하지 않을 경우에만 캐싱해주자.
    // 두번째는 자주 사용하는 데이터를 캐싱하면 좋다. 즉, 접근이 많은 데이터를 캐싱할수록 DB의 부하가 적어지므로 언제나 자주 접근되는 데이터를 캐싱해주자.
    // 이번 서비스에서는 user같은 경우에 매 API Call마다 필터를 한번 타서 그 유저가 존재하는지 안하는지 DB에서 체크를 하게 되므로 이 부분을 캐시로해주자. 또한 user data 같은 경우에는 현재 스펙상으로 변경할 수 없고,
    // 만약 user data가 변경될 수 있는 spec이 추가되더라도 post list나 alarm list나 comment list처럼 빈번하게 변화가 발생하는 부분이 아니고 그런 데이터들 보다는 적게 변화하는 데이터이므로
    // user data를 redis caching 해주자.

    // RedisTemplate에는 key와 어떤 Data를 caching할지 Data Type(언제나 Entity(=DAO)말고, DTO를 적어두자)으로 정해야한다.
    @Bean
    public RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory redisConnectionFactory) { // Bean으로 위에서 retun된 factory를 그대로 가져오게 된다.
        RedisTemplate<String, User> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer()); // key가 String type이므로 String Serializer로 넣어주자.
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<User>(User.class)); // value가 User type이므로 이 Object serializer로 넣어주자.
        return redisTemplate;
    }
}
