package nz.co.searchwellington;

import com.google.common.collect.Maps;
import nz.co.searchwellington.commentfeeds.detectors.CommentFeedDetector;
import nz.co.searchwellington.commentfeeds.detectors.GenericCommentFeedDetector;
import nz.co.searchwellington.controllers.LoggedInUserFilter;
import nz.co.searchwellington.controllers.RssUrlBuilder;
import nz.co.searchwellington.controllers.admin.AdminUrlBuilder;
import nz.co.searchwellington.filters.RequestObjectLoadingFilter;
import nz.co.searchwellington.model.SiteInformation;
import nz.co.searchwellington.permissions.EditPermissionService;
import nz.co.searchwellington.urls.UrlBuilder;
import nz.co.searchwellington.utils.ColumnSplitter;
import nz.co.searchwellington.utils.EscapeTools;
import nz.co.searchwellington.views.ContentDedupingService;
import nz.co.searchwellington.views.GoogleMapsDisplayCleaner;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.velocity.VelocityAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.view.velocity.VelocityConfigurer;
import org.springframework.web.servlet.view.velocity.VelocityViewResolver;
import uk.co.eelpieconsulting.common.caching.MemcachedCache;
import uk.co.eelpieconsulting.common.dates.DateFormatter;

import java.io.IOException;
import java.util.Map;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class, VelocityAutoConfiguration.class})
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
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(requestObjectLoadingFilter);
        registration.addUrlPatterns("/*");
        registration.setName("requestObjectLoadingFilter");
        registration.setOrder(1);
        return registration;
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

    @Bean
    public DateFormatter dateFormatter() {
        return new uk.co.eelpieconsulting.common.dates.DateFormatter("Europe/London");
    }

    @Bean
    public VelocityViewResolver velocityViewResolver(
            AdminUrlBuilder adminUrlBuilder,
            ColumnSplitter columnSplitter,
            ContentDedupingService contentDedupingService,
            DateFormatter dateFormatter,
            EditPermissionService editPermissionService,
            GoogleMapsDisplayCleaner googleMapsDisplayCleaner,
            LoggedInUserFilter loggedInUserFilter,
            RssUrlBuilder rssUrlBuilder,
            SiteInformation siteInformation,
            UrlBuilder urlBuilder) {
        final VelocityViewResolver viewResolver = new VelocityViewResolver();
        viewResolver.setCache(true);
        viewResolver.setPrefix("");
        viewResolver.setSuffix(".vm");
        viewResolver.setContentType("text/html;charset=UTF-8");

        final Map<String, Object> attributes = Maps.newHashMap();
        attributes.put("adminUrlBuilder", adminUrlBuilder);
        attributes.put("columnSplitter", columnSplitter);
        attributes.put("contentDeduper", contentDedupingService);
        attributes.put("dateFormatter", dateFormatter);
        attributes.put("editPermissionService", editPermissionService);
        attributes.put("googleMapCleaner", googleMapsDisplayCleaner);
        attributes.put("loggedInUserFilter", loggedInUserFilter);   // TODO not very functional
        attributes.put("rssUrlBuilder", rssUrlBuilder);
        attributes.put("siteInformation", siteInformation);
        attributes.put("urlBuilder", urlBuilder);
        viewResolver.setAttributesMap(attributes);
        return viewResolver;
    }

    @Bean
    public VelocityConfigurer velocityConfigurer() {
        final VelocityConfigurer vc = new VelocityConfigurer();
        final Map<String, Object> velocityPropertiesMap = Maps.newHashMap();
        velocityPropertiesMap.put(Velocity.OUTPUT_ENCODING, "UTF-8");
        velocityPropertiesMap.put(Velocity.INPUT_ENCODING, "UTF-8");
        velocityPropertiesMap.put(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityPropertiesMap.put("eventhandler.referenceinsertion.class", "org.apache.velocity.app.event.implement.EscapeHtmlReference");
        vc.setVelocityPropertiesMap(velocityPropertiesMap);
        return vc;
    }

}
