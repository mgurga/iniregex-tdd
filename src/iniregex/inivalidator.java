package iniregex;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class inivalidator {
	
	private static final String commentregex = ";.*";
	private static final String sectionheaderregex = "\\[.*\\]";
	private static final String propertyvalueregex = ".*=[ \\_0-9_\\w.//(),\\!\\@\\#]*";
	
	// checks based on rough ini standard
	// [section]
	// property=value
	// ; comment
	
	private String[] filelines;
	private LinkedHashMap<String, LinkedHashMap<String, String>> inihm = new LinkedHashMap<String, LinkedHashMap<String, String>>();
	private ArrayList<String> errors = new ArrayList<String>();
	private ArrayList<Integer> lineerrors = new ArrayList<Integer>();
	private boolean DEBUG = false;
	
	public inivalidator(String[] file) {
		this.filelines = file;
	}
	
	public inivalidator(String file) {
		this.filelines = file.split("\n");
	}
	
	public boolean isValid() {
		boolean out = true;
		for(int i = 0; i < filelines.length; i++) {
			if(!isLineValid(filelines[i], i)) {
				out = false;
			}
		}
		
		if(out)
			loadData();
		
		return out;
	}
	
	private void loadData() {
		String curSection = "";
		boolean readingSection = false;
		LinkedHashMap<String, String> properties = new LinkedHashMap<String, String>();
		
		for(int i = 0; i < filelines.length; i++) {
			String line = filelines[i];
			if(!readingSection && line.matches(sectionheaderregex)) {
				// section header detected, starting reading properties
				readingSection = true;
				curSection = line.replaceAll("\\[|\\]", "");
				properties = new LinkedHashMap<String, String>();
			}
			
			if(readingSection && line.matches(propertyvalueregex)) {
				String[] propval = line.split("=");
				properties.put(propval[0], propval[1]);
			}
			
			if(readingSection && line.equals("")) {
				// empty line is section reading terminator
				readingSection = false;
				inihm.put(curSection, properties);
			}
			
			if(readingSection && line.matches(sectionheaderregex)) {
				// start reading section directly after property
				inihm.put(curSection, properties);
				curSection = line.replaceAll("\\[|\\]", "");
				properties = new LinkedHashMap<String, String>();
			}
		}
		
		if(readingSection) {
			inihm.put(curSection, properties);
		}
	}
	
	private boolean isLineValid(String line, int linenum) {
		if(line.matches(sectionheaderregex)) {
			// line is a section header
			if(DEBUG)
				System.out.println(linenum + " is section header");
			return true;
		} else if(line.matches(propertyvalueregex)) {
			// line is property=value 
			if(DEBUG)
				System.out.println(linenum + " is property value");
			return true;
		} else if(line.matches(commentregex)) {
			// line is a comment
			if(DEBUG)
				System.out.println(linenum + " is a comment");
			return true;
		} else if(line.equals("")) {
			// line is empty
			if(DEBUG)
				System.out.println(linenum + " is empty line");
			return true;
		} else {
			findError(line, linenum);
			return false;
		}
	}
	
	private void findError(String line, int linenumber) {
		errors.add("line #" + linenumber + " '" + line + "' is not valid");
		lineerrors.add(linenumber);
	}
	
	public String[] listSections() {
		String[] keyarr = new String[inihm.keySet().size()];
		for(int i = 0; i < keyarr.length; i++) {
			keyarr[i] = inihm.keySet().toArray()[i].toString();
		}
		return keyarr;
	}
	
	public String[] listSectionKeys(String section) {
		String[] keyarr = new String[inihm.get(section).keySet().size()];
		for(int i = 0; i < keyarr.length; i++) {
			keyarr[i] = inihm.get(section).keySet().toArray()[i].toString();
		}
		return keyarr;
	}
	
	public String getSectionKey(String section, String key) {
		return inihm.get(section).get(key);
	}
	
	public String[] getErrors() {
		return errors.toArray(new String[0]);
	}
	
	public int[] getErrorLines() {
		int[] out = new int[lineerrors.size()];
		
		for(int i = 0; i < lineerrors.size(); i++) {
			out[i] = lineerrors.get(i);
		}
		
		return out;
	}
	
	public String toString() {
		String out = "";
		String[] sections = listSections();
		for(String i : sections) {
			String[] keys = listSectionKeys(i);
			out += i + "\n";
			for(int j = 0; j < keys.length; j++) {
				if(j < keys.length - 1) {
					out += "┣" + keys[j] + ":" + getSectionKey(i, keys[j]) + "\n";
				} else {
					out += "┗" + keys[j] + ":" + getSectionKey(i, keys[j]) + "\n";
				}
			}
		}
		return out;
	}
}
