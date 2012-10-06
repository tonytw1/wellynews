package nz.co.searchwellington;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import nz.co.searchwellington.controllers.models.ContentModelBuilderService;
import nz.co.searchwellington.controllers.models.TagModelBuilder;
import nz.co.searchwellington.feeds.rss.RssNewsitemPrefetcher;
import nz.co.searchwellington.feeds.rss.RssPrefetcherTest;
import nz.co.searchwellington.filters.RequestFilter;

import org.junit.Test;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.sun.tools.internal.ws.processor.modeler.annotation.ModelBuilder;

public class SpringContextTest {

	@Test
	public void canLoadContext() throws Exception {
		 ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		 System.out.println(context);
		 assertNotNull(context);
		 
		 String[] beanNamesForType = context.getBeanNamesForType(ModelBuilder.class);
		 System.out.println(beanNamesForType);
		 System.out.println(beanNamesForType.length);
		
		 final List<String> beans = Arrays.asList(context.getBeanDefinitionNames());
		 Collections.sort(beans);
		 for (String bean : beans) {
			 System.out.println(bean);
		 }
		 
		System.out.println(context.getAutowireCapableBeanFactory().autowire(TagModelBuilder.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true));
	}
	
	@Test
	public void canAutowire() throws Exception {
		final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");		
		Object autowire = context.getAutowireCapableBeanFactory().autowire(RssNewsitemPrefetcher.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);
		System.out.println(autowire);
		assertNotNull(autowire);
	}
	
	//@Test
	public void canAutowireListParameters() throws Exception {
		final ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		final RequestFilter requestFilter = (RequestFilter) context.getAutowireCapableBeanFactory().autowire(TagModelBuilder.class, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, true);		
		assertNotNull(requestFilter.getFilters());
	}
	
}
