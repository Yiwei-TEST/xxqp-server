package com.sy599.game.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * 对指定的分词进行过滤
 * @author taohuiliang
 * @date 2013-7-22
 * @version v1.0
 */
public class KeyWordsFilter{
	private DFANode dfaEntrance;
	private ArrayList<DFANode> dfaNodes;
	private HashSet<Character> ignoreChars;
	private char subChar;
	private static KeyWordsFilter _inst = new KeyWordsFilter();

	private KeyWordsFilter(){
		dfaEntrance = new DFANode();
		dfaNodes = new ArrayList<DFANode>();
		ignoreChars = new HashSet<Character>();
	}
	
	@SuppressWarnings("unchecked")
	public void initData(String path){
		try{
			
			List keywords = FileUtils.readLines(new File(path), "GB2312");
			String keys[] = new String[keywords.size()];
			for (int i = 0; i < keywords.size(); i++){
				String key = (String)keywords.get(i);
				key = key.trim();
				keys[i] = key;
			}
			char ignores[] = {' ', '\t', '-'};
			initialize(keys, ignores, '*');
		}catch (IOException e){
			e.printStackTrace();
			LogUtil.e("initData",e);
		}
	}
	
	public static KeyWordsFilter getInstance(){
		return _inst;
	}

	public void clear(){
		
		for(DFANode node:dfaNodes){
			node.dfaTransition.clear();
		}

		dfaNodes.clear();
		ignoreChars.clear();
	}

	private boolean initialize(String keyWords[], char ignore[], char substitute){
		clear();
		for (int i = 0; i < ignore.length; i++)
			ignoreChars.add(Character.valueOf(ignore[i]));

		subChar = substitute;
		for(String keyWord:keyWords){
			char patternTextArray[] = keyWord.toCharArray();
			DFANode currentDFANode = dfaEntrance;
			for (int i = 0; i < patternTextArray.length; i++){
				if (!ignoreChars.contains(Character.valueOf(patternTextArray[i]))){
					if (currentDFANode.dfaTransition.containsKey(Character.valueOf(Character.toLowerCase(patternTextArray[i])))){
						currentDFANode = (DFANode)currentDFANode.dfaTransition.get(Character.valueOf(Character.toLowerCase(patternTextArray[i])));
					}else{
						DFANode newDFANode = new DFANode();
						currentDFANode.dfaTransition.put(Character.valueOf(Character.toLowerCase(patternTextArray[i])), newDFANode);
						dfaNodes.add(newDFANode);
						currentDFANode = newDFANode;
					}
				}
					
			}
			
			currentDFANode.canExit = true;
		}
			

		return true;
	}

	public String filt(String s){
		char input[] = s.toCharArray();
		char result[] = s.toCharArray();
		int killto = -1;
		for (int i = 0; i < input.length; i++){
			killto = -1;
			DFANode currentDFANode = dfaEntrance;
			for (int j = i; j < input.length; j++){
				if (ignoreChars.contains(Character.valueOf(input[j])))
					continue;
				if (!currentDFANode.dfaTransition.containsKey(Character.valueOf(Character.toLowerCase(input[j]))))
					break;
				currentDFANode = (DFANode)currentDFANode.dfaTransition.get(Character.valueOf(Character.toLowerCase(input[j])));
				if (currentDFANode.canExit)
					killto = j;
			}

			if (killto != -1){
				for (int j = i; j <= killto; j++)
					if (!ignoreChars.contains(Character.valueOf(input[j])))
						result[j] = subChar;

			}
		}

		return String.valueOf(result);
	}

	class DFANode{

		public HashMap<Character,DFANode> dfaTransition;
		public boolean canExit;
		

		public DFANode(){

			dfaTransition = new HashMap<Character,DFANode>();
			canExit = false;
		}

	}

}
