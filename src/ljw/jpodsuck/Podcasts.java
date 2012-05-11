package ljw.jpodsuck;
import java.util.TreeMap;
import java.util.Map;

public class Podcasts implements PodcastsInterface {
	protected Map<String, Item> podCasts = new TreeMap<String, Item>();
	Item currentItem; 
	protected String channelTitle;
	
	@Override
	public String getChannelTitle() {
		return channelTitle;
	}

	public Podcasts() {
	}

	@Override
	public void accept(PodcastVisitor visitor) {
		for (Map.Entry<String, Item> i: podCasts.entrySet()) {
			visitor.visit(i.getValue());
		}
	}
	
	@Override
	public void contextCreateItem() {
		currentItem = new Item();	
	}

	@Override
	public void setChannelTitle(String channelTitle) {
		this.channelTitle = channelTitle;		
	}

	@Override
	public void addContextPubDate(String pubDate) {
		if (currentItem == null)
			throw new RuntimeException("item is null");
		currentItem.pubDate = pubDate;
		
	}

	@Override
	public void addContextTitle(String title) {
		if (currentItem == null)
			throw new RuntimeException("item is null");
		currentItem.title = title;
		
	}


	@Override
	public void addContextUrlInfo(String url, int length) {
		if (currentItem == null)
			throw new RuntimeException("item is null");
		currentItem.url = url;
		currentItem.length = length;
	}

	@Override
	public void confirmContextItem() {
		podCasts.put(currentItem.url, currentItem);
		currentItem = null;
		
	}


}
