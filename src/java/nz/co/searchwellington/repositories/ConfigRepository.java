package nz.co.searchwellington.repositories;

import nz.co.searchwellington.config.Config;


public interface ConfigRepository {

    public String getFeedBurnerWidget();

    public String getStatsTracking();

    public Config getConfig();

    public void saveConfig(Config config);

    public String getFlickrPoolGroupId();

    public boolean getUseClickThroughCounter();

}
