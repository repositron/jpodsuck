package ljw.jpodsuck;


public interface PodcastsInterface {
	void contextCreateItem();
	void setChannelTitle(String channelTitle);
	void addContextPubDate(String pubDate);
	void addContextTitle(String title);
	void addContextUrlInfo(String url, int length);
	void confirmContextItem();
	void accept(PodcastVisitor visitor);
	String getChannelTitle();
}
