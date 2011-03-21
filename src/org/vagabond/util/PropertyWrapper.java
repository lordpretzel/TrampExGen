package org.vagabond.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

public class PropertyWrapper extends Properties {

	static Logger log = Logger.getLogger(PropertyWrapper.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5360281091166986337L;
	
	private String prefix = null;
	
	public PropertyWrapper (Properties props) {
		super(props);
	}
	
	public PropertyWrapper (String fileName) throws FileNotFoundException, IOException {
		super();
		super.load(new FileReader(fileName));
	}
	
	public PropertyWrapper (File inFile, boolean xml) throws FileNotFoundException, IOException {
		super();
		if (xml)
			super.loadFromXML(new FileInputStream(inFile));
		else 
			super.load(new FileInputStream(inFile));
	}

	
	public PropertyWrapper() {
		super();
	}

	public void addFromXMLFile (File inFile, String prefix) 
			throws FileNotFoundException, IOException {
		this.prefix = prefix;
		addFromXMLFile(inFile);
		resetPrefix();
	}
	
	public void addFromXMLFile (File inFile) 
			throws FileNotFoundException, IOException {
		log.debug("load from file <" + inFile.getAbsolutePath() + "> with prefix <" 
					+ prefix + ">");
		PropertyWrapper sub = new PropertyWrapper();
		sub.loadFromXML(new FileInputStream(inFile));
		addAll(sub);
	}
	
	public void addFromFile (File inFile, String prefix) 
			throws FileNotFoundException, IOException {
		this.prefix = prefix;
		addFromFile(inFile);
		resetPrefix();
	}
	
	public void addFromFile (File inFile) 
			throws FileNotFoundException, IOException {
		log.debug("load from file <" + inFile.getAbsolutePath() + "> with prefix <" 
					+ prefix + ">");
		PropertyWrapper sub = new PropertyWrapper(inFile, false);
		addAll(sub);
	}
	
	public void addAll (PropertyWrapper wrap) {
		Set<String> keys = wrap.stringPropertyNames();
		
		for (String key: keys) {
			log.debug("add key <" + key + "> with value <" + wrap.getProperty(key) + ">");
			this.setProperty(key, wrap.getProperty(key)); 
		}
	}
	
	public void resetPrefix () {
		prefix = null;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	
	@Override
	public String getProperty (String name) {
		if (prefix == null) {
			return super.getProperty(name);
		}
		return super.getProperty(prefix + "." + name);
	}
	
	@Override
	public boolean containsKey (Object key) {
		if (prefix == null)
			return super.containsKey(key);
		return super.containsKey(prefix + "." + ((String) key));  
	}
	
	public Object setProperty (String key, String value) {
		if (prefix == null)
			return super.setProperty(key, value);
		else
			return super.setProperty(prefix + "." + key, value);
	}
	
	public boolean setPropertyIfUnset (String key, String value) {
		if (this.containsKey(key)) {
			return false;
		}
		this.setProperty(key, value);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Enumeration<Object> keys() {
		Enumeration<Object> superKeys;
		List<Object> preKeys;
		
		superKeys = super.keys();
		if (prefix == null)
			return superKeys;
		preKeys = new ArrayList<Object> ();
		for(Object key = superKeys.nextElement(); superKeys.hasMoreElements();  
				key = superKeys.nextElement()) 
			if (((String) key).startsWith(prefix))
				preKeys.add(key);
		
		return (Enumeration<Object>) preKeys;
	}
	
	public Set<Object> keySet () {
		Set<Object> superKeys;
		Set<Object> preKeys;
		
		superKeys = super.keySet();
		if (prefix == null)
			return superKeys;
		preKeys = new HashSet<Object> ();
		for(Object key: superKeys)
			if (((String) key).startsWith(prefix))
				preKeys.add(key);
		
		return preKeys;
	}
	
	public String getString (String name) {
		return this.getProperty(name);
	}
	
	public boolean getBool (String name) {
		return Boolean.parseBoolean(this.getProperty(name));
	}
	
	public float getFloat (String name) {
		return Float.parseFloat(this.getProperty(name));
	}
	
	public int getInt (String name) {
		return Integer.parseInt(this.getProperty(name));
	}
	
	public long getLongProperty (String key) {
		return Long.parseLong(this.getProperty(key));
	}
	
	public boolean containsKeyOrSub (String key) {
		String keyName;
		
		if (this.containsKey(key))
			return true;
		
		for(Object testKey : this.keySet()) {
			keyName = (String) testKey;
			
			if (keyName.startsWith(key + "."))
				return true;
		}
		
		return false;
	}
	
	public String[] getArrayProperty (String key) {
		String value;
		String[] result;
		
		value = this.getProperty(key);
		if(value == null || value.equals("")) {
			return new String[0];
		}
		
		value = value.replaceAll("\\n", "");
		
		result = value.split(",");  
		
		log.debug("get Array Property: " + result);
		
		return result;
	}
	
	public int[] getIntArrayProperty (String key) {
		String[] values;
		int[] result;
		
		values = getArrayProperty (key);
		result = new int[values.length];
		
		for(int i = 0; i < values.length; i++) {
			result[i] = Integer.parseInt(values[i]);
		}
		
		return result;
	}
	
	public Vector<String> getVectorProperty (String key) {
		String[] elements;
		String value;
		Vector<String> result;
		
		result = new Vector<String> ();
		
		value = this.getProperty(key);
		if (value.equals("")) {
			return result;
		}
		
		value = value.replaceAll("\\n", "");
		
		elements = value.split(",");
		
		for (int i = 0; i < elements.length; i++) {
			result.add(elements[i]);
		}
		
		log.debug("get Vector Property (" + key + "):" + result.toString());
		
		return result;
	}
	
	public Properties getPropertiesProperty (String key) {
		return getPropertiesProperty(key, null);
	}
	
	public Properties getPropertiesProperty (String key, String prefix) {
		PropertyWrapper result;
		String[] keyValues;
		
		result = new PropertyWrapper ();
		result.setPrefix(prefix);
		keyValues = this.getArrayProperty(key);
		
		for (int i = 0; i < keyValues.length; i++) {
			result.setProperty(keyValues[i].split("=")[0], keyValues[i].split("=")[1]);
		}
		
		log.debug("get PropertiesProperty (" + key + "): " + result.toString());
		
		return result;
	}
	
	public QueryTemplate getQueryTemplate (String key) {
		return new QueryTemplate(this.getProperty(key));
	}
	
	public int getSubPropertiesNum () {
		return getSubPropertiesNum(prefix);
	}
	
	public int getSubPropertiesNum (String prefix) {
		return this.keySet().size();
	}
}
