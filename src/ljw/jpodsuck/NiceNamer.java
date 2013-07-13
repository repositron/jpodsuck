package ljw.jpodsuck;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;


class NiceNamer {
	private Map<String, String> abbreviatorLookup;  
	public NiceNamer(Map<String, String> abbreviatorLookup) {
		this.abbreviatorLookup = abbreviatorLookup;
	}
	public String makeTitle(String longTitle)
	{
		// space hyphen space used to separate track title from album name
		int separator = longTitle.indexOf(" - ");   
		if (separator == -1) {
			return longTitle;
		}
		StringBuilder niceTitle = new StringBuilder();
		for (Map.Entry<String, String>  entry : abbreviatorLookup.entrySet()) {
			String l = entry.getKey();
			if (longTitle.startsWith(l))
			{
				niceTitle.append(entry.getValue());
				niceTitle.append(longTitle.substring(entry.getKey().length()));
				break;
			}
		}
	
		if (niceTitle.length() == 0) {
			niceTitle.append(longTitle);
			return longTitle;
		}
		return niceTitle.toString();
	}
}

class Abbreviator {
	private Map<String, String> lookup = new HashMap<String, String>(); 
	public void add(String longName, String abbreviatedName)
	{
		lookup.put(longName, abbreviatedName);
	}
	public ImmutablePair<Boolean, String> getShortName(String longName)
	{
		String shortName = lookup.get(longName);
		if (shortName == null)
			return new ImmutablePair<Boolean, String>(false, "");
		else
			return new ImmutablePair<Boolean, String>(true, shortName);
	}
}
