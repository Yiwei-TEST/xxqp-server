package com.sy.sanguo.common.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import org.dom4j.io.SAXReader;

/**
 * 依赖包dom4j XML工具类
 *
 * @author wqc
 * @date 2012-7-23
 * @version v1.0
 */
public class XmlUtil {

	private static String ROOT = "request";
	private static String LIST = "list";

	private XmlUtil() {
	}

	public static String fromMap(Map<String, Object> map) {
		return fromMap(map, null);
	}

	/**
	 * Map-XML
	 *
	 * @param map
	 * @return String xml
	 */
	public static String fromMap(Map<String, Object> map, String rootName) {
		Element root = null;
		if (!StringUtils.isBlank(rootName)) {
			root = new DOMElement(rootName);
		} else {
			root = new DOMElement(ROOT);
		}
		for (String k : map.keySet()) {
			addElement(root, k, map.get(k));
		}
		return root.asXML();
	}

	/**
	 * XML-Map
	 *
	 * @param xml
	 * @return Map<String,Object>
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> toMap(String xml) {
		Element root;
		try {
			root = new SAXReader().read(new StringReader(xml)).getDocument().getRootElement();
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
		Map<String, Object> map = new HashMap<String, Object>();
		Iterator<Element> it = root.elementIterator();
		while (it.hasNext()) {
			Element e = it.next();
			addObject(map, e);
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	private static void addObject(Map<String, Object> map, Element e) {
		if (e.getName().trim().equals(LIST)) {
			addList(map, e.elements());
			return;
		}
		if (e.elements().size() > 0) {
			addMap(map, e);
			return;
		}
		String k = e.getName();
		String v = e.getTextTrim();
		if (k == null)
			k = "null";
		if (v == null)
			v = "";
		map.put(k, v);
	}

	@SuppressWarnings("unchecked")
	private static void addMap(Map<String, Object> map, Element e) {
		Map<String, Object> m = new HashMap<String, Object>();
		Iterator<Element> it = e.elements().iterator();
		while (it.hasNext()) {
			Element element = it.next();
			addObject(m, element);
		}
		map.put(e.getName(), m);
	}

	private static void addList(Map<String, Object> map, Collection<Element> c) {
		List<Object> list = new ArrayList<Object>();
		for (Element element : c) {
			Map<String, Object> e = new HashMap<String, Object>();
			addObject(e, element);
			list.add(e);
		}
		map.put(LIST, list);
	}

	@SuppressWarnings("unchecked")
	private static void addElement(Element root, String key, Object o) {
		if (o instanceof Map) {
			try {
				Map map = (Map) o;
				addMap(root, key, map);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return;
		}
		if (o instanceof List) {
			try {
				List list = (List) o;
				addList(root, key, list);
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return;
		}
		if (key == null)
			key = "null";
		String v = "";
		if (o != null)
			v = o.toString();
		Element ele = new DOMElement(key);
		ele.addCDATA(v);
		root.add(ele);
	}

	@SuppressWarnings("unchecked")
	private static void addMap(Element root, String key, Map map) {
		if (key == null)
			key = "null";
		Element ele = new DOMElement(key);
		for (Object o : map.keySet()) {
			addElement(ele, o.toString(), map.get(o));
		}
		root.add(ele);
	}

	@SuppressWarnings("unchecked")
	private static void addList(Element root, String key, List list) {
		if (key == null)
			key = "null";
		Element ele = new DOMElement(key);
		for (Object o : list) {
			addElement(ele, LIST, o);
		}
		root.add(ele);
	}

}
