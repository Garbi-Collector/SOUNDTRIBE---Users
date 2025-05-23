package soundtribe.soundtribeusers.configs;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        // Cache de 2 hora
        Cache getAllUsersCache = new CaffeineCache("getAllUsersCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.HOURS)
                        .maximumSize(2000)
                        .build());

        // Cache de 2 hora
        Cache userDescriptionCache = new CaffeineCache("userDescriptionCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.HOURS)
                        .maximumSize(2000)
                        .build());

        // Cache de 2 hora
        Cache userGetCache = new CaffeineCache("userGetCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.HOURS)
                        .maximumSize(2000)
                        .build());
        // Cache de 2 hora
        Cache ListuserGetCache = new CaffeineCache("ListuserGetCache",
                Caffeine.newBuilder()
                        .expireAfterWrite(2, TimeUnit.HOURS)
                        .maximumSize(2000)
                        .build());


        // Agregá todos los caches al manager
        cacheManager.setCaches(Arrays.asList(
                getAllUsersCache,
                userDescriptionCache,
                userGetCache,
                ListuserGetCache
        ));

        return cacheManager;
    }
}
