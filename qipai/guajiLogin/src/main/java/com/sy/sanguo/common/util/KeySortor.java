package com.sy.sanguo.common.util;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Comparator;

public class KeySortor implements Comparator<Object> {
	private Collator collator = Collator.getInstance();
	
	public KeySortor(){
		
	}
	@Override
	public int compare(Object o1, Object o2) {
		CollationKey key1 = collator.getCollationKey(o1.toString());
		CollationKey key2 = collator.getCollationKey(o2.toString());
		return key1.compareTo(key2);
	}
}
