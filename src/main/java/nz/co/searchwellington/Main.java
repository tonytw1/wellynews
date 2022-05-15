package nz.co.searchwellington;

import com.google.common.collect.Maps;
import nz.co.searchwellington.commentfeeds.detectors.*;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder;
import nz.co.searchwellington.filters.RequestObjectLoadingFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.permissions.EditPermissionService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.utils.EscapeTools;
import nz.co.searchwellington.views.ColumnSplitter;
import nz.co.searchwellington.views.VelocityViewResolver;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.spring.VelocityEngineFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.dates.DateFormatter;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@ComponentScan("nz.co.searchwellington,uk.co.eelpieconsulting.common")
@Configuration
public class Main {

    private final static Logger log = Logger.getLogger(Main.class);

    private static ApplicationContext ctx;

    public static void main(String[] args) {
        ctx = SpringApplication.run(Main.class, args);
    }

    @Autowired
    private RequestObjectLoadingFilter requestObjectLoadingFilter;

    @Bean
    public CommentFeedDetector newswireCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://www.newswire.co.nz/\\d{4}/\\d{2}/.*?/feed/$");
        return detector;
    }
    @Bean
    public CommentFeedDetector aucklandTrainsCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://www.aucklandtrains.co.nz/\\d{4}/\\d{2}/\\d{2}/.*?/feed/$");
        return detector;
    }

    @Bean
    public CommentFeedDetector tepapaBlogCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://blog.tepapa.govt.nz/\\d{4}/\\d{2}/\\d{2}/.*?/feed/$");
        return detector;
    }

    @Bean
    public CommentFeedDetector wellingtonistaCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://wellingtonista.com/crss/node/\\d+$");
        return detector;
    }

    @Bean
    public CommentFeedDetector yearMonthCommentFeedDetector() {
        GenericCommentFeedDetector detector = new GenericCommentFeedDetector();
        detector.setRegex("^http://.*?/\\d{4}/\\d{2}/.*?$");
        return detector;
    }
    @Bean
    public CommentFeedDetector dateRegexCommentFeedDetector() {
        return new DateRegexCommentFeedDetector();
    }

    @Bean("feedReaderTaskExecutor")
    @Primary    // TODO work out why this is needed
    public TaskExecutor feedReaderTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(50000);
        return threadPoolTaskExecutor;
    }

    @Bean("linkCheckerTaskExecutor")
    @Primary    // TODO work out why this is needed
    public TaskExecutor linkCheckerTaskExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(5);
        threadPoolTaskExecutor.setMaxPoolSize(10);
        threadPoolTaskExecutor.setQueueCapacity(50000);
        return threadPoolTaskExecutor;
    }

    @Bean
    public MemcachedCache memcachedCache(@Value("${memcached.urls}") String memcacheUrl) throws IOException {
        return new MemcachedCache(memcacheUrl);
    }

    @Bean
    public DateFormatter dateFormatter() {
        return new uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London");
    }

    @Bean
    public VelocityViewResolver velocityViewResolver(
            AdminUrlBuilder adminUrlBuilder,
            ColumnSplitter columnSplitter,
            DateFormatter dateFormatter,
            EditPermissionService editPermissionService,
            RssUrlBuilder rssUrlBuilder,
            SiteInformation siteInformation,
            UrlBuilder urlBuilder) {
        final VelocityViewResolver viewResolver = new VelocityViewResolver();
        viewResolver.setCache(true);
        viewResolver.setSuffix(".vm");
        viewResolver.setContentType("text/html;charset=UTF-8");
        final Map<String, Object> attributes = Maps.newHashMap();
        attributes.put("adminUrlBuilder", adminUrlBuilder);
        attributes.put("columnSplitter", columnSplitter);
        attributes.put("dateFormatter", dateFormatter);
        attributes.put("editPermissionService", editPermissionService);
        attributes.put("escape", new EscapeTools());
        attributes.put("rssUrlBuilder", rssUrlBuilder);
        attributes.put("siteInformation", siteInformation);
        attributes.put("urlBuilder", urlBuilder);
        viewResolver.setAttributesMap(attributes);
        return viewResolver;
    }

    @Bean("velocityEngine")
    public VelocityEngineFactoryBean velocityEngineFactoryBean() {
        VelocityEngineFactoryBean velocityEngineFactory= new VelocityEngineFactoryBean();
        Properties vp = new Properties();
        vp.setProperty(Velocity.INPUT_ENCODING, "UTF-8");
        vp.setProperty(Velocity.EVENTHANDLER_REFERENCEINSERTION, "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        vp.setProperty("resource.loader", "class");
        vp.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        vp.setProperty("velocimacro.library", "spring.vm");
        velocityEngineFactory.setVelocityProperties(vp);
        return velocityEngineFactory;
    }

}
