package net.sy599.common.security;

import java.io.BufferedReader;
import java.io.InputStreamReader;



/**
 * DES使用(Main方法里面测试)
 * @author kalman
 */
public class Test {

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception 
	{
		String str = "yly-ljp";
		BufferedReader br=null;
		System.out.println("请输入需要加密的原文(输入【end】 结束):");
		
		while(true){
		 br=new BufferedReader(new InputStreamReader(System.in));//关键 从键盘读入
		 str = br.readLine();
		 
		 if(str.equals("end")){
			 break;
		 }
		
		System.out.println("加密前:" + str);
		SecuritConstant des = new SecuritConstantImpl();
		String str1 = des.encrypt(str);
		System.out.println("加密后:" + str1);
		String str2 = des.decrypt(str1);
		System.out.println("解密后:" + str2);
		}
		
	}

}
