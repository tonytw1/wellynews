package nz.co.searchwellington;

import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector;
import nz.co.searchwellington.commentfeeds.detectors.GenericCommentFeedDetector;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;

import java.io.IOException;

@EnableScheduling
@EnableAutoConfiguration
@ComponentScan("nz.co.searchwellington,uk.co.eelpieconsulting.common")
@Configuration
public class Main {

    private final static Logger log = Logger.getLogger(Main.class);

    private static ApplicationContext ctx;

    public static void main(String[] args) {
        ctx = SpringApplication.run(Main.class, args);
    }

    @Bean
    public CommentFeedDetector newswiresCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://www.newswire.co.nz/\\d{4}/\\d{2}/.*?/feed/$");
        return detector;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(4);
        threadPoolTaskExecutor.setMaxPoolSize(5);
        threadPoolTaskExecutor.setQueueCapacity(50000);
        return threadPoolTaskExecutor;
    }

    @Bean
    public MemcachedCache memcachedCache(@Value("${memcached.urls}") String memcacheUrl) throws IOException {
        return new MemcachedCache(memcacheUrl);
    }

}
