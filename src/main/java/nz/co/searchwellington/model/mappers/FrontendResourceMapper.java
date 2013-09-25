package nz.co.searchwellington.model.mappers;

import java.util.List;

import nz.co.searchwellington.model.Feed;
import nz.co.searchwellington.model.Geocode;
import nz.co.searchwellington.model.Newsitem;
import nz.co.searchwellington.model.Resource;
import nz.co.searchwellington.model.Tag;
import nz.co.searchwellington.model.Twit;
import nz.co.searchwellington.model.UrlWordsGenerator;
import nz.co.searchwellington.model.frontend.FrontendFeed;
import nz.co.searchwellington.model.frontend.FrontendImage;
import nz.co.searchwellington.model.frontend.FrontendNewsitem;
import nz.co.searchwellington.model.frontend.FrontendResource;
import nz.co.searchwellington.model.frontend.FrontendTag;
import nz.co.searchwellington.model.frontend.FrontendTweet;
import nz.co.searchwellington.tagging.TaggingReturnsOfficerService;
import nz.co.searchwellington.views.GeocodeToPlaceMapper;

import org.elasticsearch.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FrontendResourceMapper {
	
	private final TaggingReturnsOfficerService taggingReturnsOfficerService;
	private final UrlWordsGenerator urlWordsGenerator;
	private final GeocodeToPlaceMapper geocodeToPlaceMapper;
	
	@Autowired
	public FrontendResourceMapper(TaggingReturnsOfficerService taggingReturnsOfficerService,
			UrlWordsGenerator urlWordsGenerator,
			GeocodeToPlaceMapper geocodeToPlaceMapper) {
		this.taggingReturnsOfficerService = taggingReturnsOfficerService;
		this.urlWordsGenerator = urlWordsGenerator;
		this.geocodeToPlaceMapper = geocodeToPlaceMapper;
	}

	public FrontendResource createFrontendResourceFrom(Resource contentItem) {
		FrontendResource frontendContentItem = new FrontendResource();
		if (contentItem.getType().equals("N")) {
			final Newsitem contentItemNewsitem = (Newsitem) contentItem;
			FrontendNewsitem frontendNewsitem = new FrontendNewsitem();
			frontendNewsitem.setPublisherName(contentItemNewsitem.getPublisherName());
			frontendNewsitem.setAcceptedFromFeedName(contentItemNewsitem.getFeed() != null ? contentItemNewsitem.getFeed().getName() : null);
			frontendNewsitem.setAcceptedByProfilename(contentItemNewsitem.getAcceptedBy() != null ? contentItemNewsitem.getAcceptedBy().getProfilename() : null);
			frontendNewsitem.setAccepted(contentItemNewsitem.getAccepted());
			if (contentItemNewsitem.getImage() != null) {
				frontendNewsitem.setFrontendImage(new FrontendImage(contentItemNewsitem.getImage().getUrl()));
			}
			
			if (!contentItemNewsitem.getRetweets().isEmpty()) {
				List<FrontendTweet> twitterMentions = Lists.newArrayList();
				for (Twit tweet : contentItemNewsitem.getRetweets()) {
					new FrontendTweet(tweet.getText(), tweet.getAuthor());
				}
				frontendNewsitem.setTwitterMentions(twitterMentions);
			}
			
			frontendContentItem = frontendNewsitem;
		}
		
		if (contentItem.getType().equals("F")) {
			FrontendFeed frontendFeed = new FrontendFeed();
			Feed contentItemFeed = (Feed) contentItem;
			frontendFeed.setPublisherName(contentItemFeed.getPublisherName());	
			frontendFeed.setLatestItemDate(contentItemFeed.getLatestItemDate());
			frontendContentItem = frontendFeed;
		}
		
		frontendContentItem.setId(contentItem.getId());
		frontendContentItem.setType(contentItem.getType());
		frontendContentItem.setName(contentItem.getName());
		frontendContentItem.setUrl(contentItem.getUrl());
		frontendContentItem.setDate(contentItem.getDate());
		frontendContentItem.setDescription(contentItem.getDescription());
		
		frontendContentItem.setHttpStatus(contentItem.getHttpStatus());
		frontendContentItem.setHeld(contentItem.isHeld());
		if (contentItem.getOwner() != null) {	// TODO should never be null
			frontendContentItem.setOwner(contentItem.getOwner().getProfilename());
		}
		
		frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		if (frontendContentItem.getType().equals("N")) {
			frontendContentItem.setUrlWords(urlWordsGenerator.makeUrlForNewsitem((FrontendNewsitem) frontendContentItem));
		} else if (frontendContentItem.getType().equals("F")) {
			frontendContentItem.setUrlWords("/feed/" + urlWordsGenerator.makeUrlWordsFromName(contentItem.getName()));
		}
		
		final List<FrontendTag> tags = Lists.newArrayList();
		for (Tag tag : Lists.newArrayList(taggingReturnsOfficerService.getIndexTagsForResource(contentItem))) {
			tags.add(mapTagToFrontendTag(tag));
		}
		frontendContentItem.setTags(tags);
		
		final List<FrontendTag> handTags = Lists.newArrayList();
		for (Tag tag : taggingReturnsOfficerService.getHandTagsForResource(contentItem)) {
			handTags.add(mapTagToFrontendTag(tag));
		}
		frontendContentItem.setHandTags(handTags);
		
		final Geocode contentItemGeocode = taggingReturnsOfficerService.getIndexGeocodeForResource(contentItem);
		if (contentItemGeocode != null) {
			frontendContentItem.setPlace(geocodeToPlaceMapper.mapGeocodeToPlace(contentItemGeocode));
		}		
		
		return frontendContentItem;
	}

	private FrontendTag mapTagToFrontendTag(Tag tag) {
		final FrontendTag frontendTag = new FrontendTag();
		frontendTag.setId(tag.getName());
		frontendTag.setName(tag.getDisplayName());
		return frontendTag;
	}
	
}
