package com.sy.sanguo.game.competition.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.function.BiFunction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang.NullArgumentException;

/**
 * @param
 * @author Guang.OuYang
 * @description
 * @return
 * @date 2019/9/4
 */
@AllArgsConstructor
@Getter
public class BigMap<K, E, V> {
	private final int DEFAULT_CAPACITY = 16;

	private HashMap<K, BigMapEntry<K, E, V>> bigMap;

	public BigMap() {
		bigMap = new HashMap<>(DEFAULT_CAPACITY);
	}

	BigMap(int capacity) {
		bigMap = new HashMap<>(capacity);
	}

	public void put(K k, E e, V v) {
		if (bigMap == null) {
			throw new NullArgumentException("BigMap don't init..");
		}
		this.bigMap.put(k, new BigMapEntry<>(k, e, v));
	}

	public void merge(K k, E e, V v, BiFunction<? super BigMapEntry<K, E, V>, ? super BigMapEntry<K, E, V>, ? extends BigMapEntry<K, E, V>> remappingFunction) {
		bigMap.merge(k, new BigMapEntry<>(k, e, v), remappingFunction);
	}

	public BigMapEntry<K, E, V> get(K k) {
		return bigMap.get(k);
	}

	public Iterator<BigMapEntry<K, E, V>> iterable() {
		return this.bigMap.values().iterator();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Iterator<BigMapEntry<K, E, V>> iterable = iterable();
		while (iterable.hasNext()) {
			BigMapEntry<K, E, V> next = iterable.next();
			sb.append("," + next.toString());
		}
		return sb.length() > 0 ? sb.deleteCharAt(0).toString() : sb.toString();
	}

	public BigMapEntry putIfAbsent(K k, E e, V v){
		return this.bigMap.putIfAbsent(k,  new BigMapEntry<>(k, e, v));
	}

	public boolean containsKey(K k) {
		return this.bigMap.containsKey(k);
	}

	public int size() {
		return this.bigMap.size();
	}

	@Data
	@AllArgsConstructor
	public static class BigMapEntry<K, E, V> {
		private K k;

		private E e;

		private V v;

		BigMapEntry put(K k, E e, V v) {
			this.e = e;
			this.k = k;
			this.v = v;
			return this;
		}

		public K getK() {
			return this.k;
		}

		public E getE() {
			return this.e;
		}

		public V getV() {
			return this.v;
		}

		@Override
		public String toString() {
			return "[K=" + k + " , E=" + e + " , V=" + v + "]";
		}
	}
}

