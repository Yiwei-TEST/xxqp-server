package com.sy599.game.qipai.yzwdmj.tool.hulib.core.handle;

import java.util.HashSet;
import java.util.Random;

public class Program {
	public static void print_cards(int[] cards) {
		for (int i = 0; i < 9; ++i) {
//			System.out.print(cards[i]);
//			System.out.print(",");
		}
//		System.out.println("");

		for (int i = 9; i < 18; ++i) {
//			System.out.print(cards[i]);
//			System.out.print(",");
		}
//		System.out.println("");
		for (int i = 18; i < 27; ++i) {
//			System.out.print(cards[i]);
//			System.out.print(",");
		}
//		System.out.println("");
		for (int i = 27; i < 34; ++i) {
//			System.out.print(cards[i]);
//			System.out.print(",");
		}
//		System.out.println("\n=========================================");
	}

	static HashSet<Integer> tested = new HashSet<Integer>();
	static void check_hu(int[] cards, int max_gui_index) {

		for (int i = 0; i < max_gui_index; ++i) {
			if (cards[i] > 4)
				return;
		}

		int num = 0;
		for (int i = 0; i < 9; ++i) {
			num = num * 10 + cards[i];
		}

		if (tested.contains(num)) {
			return;
		}

		tested.add(num);

		for (int i = 0; i < max_gui_index; ++i) {
			if (!Hulib.getInstance().get_hu_info(cards, 0, 0, false)) {
				print_cards(cards);
			}
		}
	}

	static void gen_auto_table_sub(int[] cards, int level) {
		for (int i = 0; i < 32; ++i) {
			int index = -1;
			if (i <= 17) {
				cards[i] += 3;
			} else if (i <= 24) {
				index = i - 18;
			} else {
				index = i - 16;
			}

			if (index >= 0) {
				cards[index] += 1;
				cards[index + 1] += 1;
				cards[index + 2] += 1;
			}

			if (level == 4) {
				check_hu(cards, 18);
			} else {
				gen_auto_table_sub(cards, level + 1);
			}

			if (i <= 17) {
				cards[i] -= 3;
			} else {
				cards[index] -= 1;
				cards[index + 1] -= 1;
				cards[index + 2] -= 1;
			}
		}
	}

	public static void test1() {
//		System.out.println("�������ֻ�ɫ\n");
		int[] cards = { 0, 0, 0, 1, 1, 1, 0, 2, 0, /* Ͱ */ 0, 0, 0, 0, 0, 0, 0, 0, 0,
				/* �� */ 0, 0, 0, 0, 0, 0, 0,0,0,/* �� */ 0, 0, 0, 0, 0, 0, 0 };

		gen_auto_table_sub(cards, 1);
		// for (int i = 0; i < 18; ++i) {
		// cards[i] = 2;
		// System.out.print("�� ");
		// System.out.println(i);
		// gen_auto_table_sub(cards, 1);
		// cards[i] = 0;
		// }
	}

	static void test_one() {
		TableMgr.getInstance().load();
		int guiIndex = 33;

		// 1Ͳ��2Ͳ��3Ͳ��4Ͳ��4Ͳ��5Ͳ��5Ͳ��6Ͳ��7Ͳ��7Ͳ��8Ͳ��8Ͳ��9Ͳ��9Ͳ
		// int[] cards = { 1, 0, 0, 0, 0, 0, 0, 0, 0,/*Ͱ*/ 1, 1, 1, 2, 1, 1, 2,
		// 2, 2,
		// /*��*/ 0, 0, 0, 0, 0, 0, 0, 0, 0,/*��*/ 0, 0, 0, 0, 0, 0, 0 };

		int[] cards = { 
			0, 0, 0, 1, 1, 1, 0, 0, 0, /* Ͱ */ 
			1, 1, 1, 0, 0, 0, 0, 0, 0, /* �� */
			2, 0, 0, 0, 0, 0, 0, 0, 0, /* �� */
			0, 0, 0, 0, 0, 0, 0 };

//		System.out.println("����1��,���:" + guiIndex);
		print_cards(cards);
		long start = System.currentTimeMillis();
		
		if (!Hulib.getInstance().get_hu_info(cards, 34, guiIndex, false)) {
//			System.out.print("����ʧ��\n");
//		} else {
//			System.out.print("���Գɹ�\n");
		}
		
//		System.out.println("use:" + (System.currentTimeMillis() - start));
	}

	public static void main(String[] args) {

		ak_test();
//		test_one();
	}
	
	private static Random random = new Random();
	private static void ak_test()
	{
//		System.out.print("test hulib begin...\n");

		TableMgr.getInstance().load();
		
		int lose = 0;
		int win = 0;
		long start = System.currentTimeMillis();
		for (int i = 0 ; i < 1000000 ; i++)
		{
			int[] cards = new int[34];
			int cardNum = 0;
			while (cardNum < 14)
			{
				if(cards[random.nextInt(cards.length)] < 4)
				{
					cards[random.nextInt(cards.length)]++;
					cardNum++;
				}
			}
			int guiIndex =  random.nextInt(34);
// 			System.out.println("����1��,���:" + guiIndex);
 			
// 			print_cards(cards);
			if (!Hulib.getInstance().get_hu_info(cards, 34, guiIndex, false)) {
// 				System.out.print("����ʧ��\n");
				lose++;
			} else {
// 				System.out.print("���Գɹ�\n");
//				System.out.println("����1��,���:" + guiIndex);
//				print_cards(cards);
				win++;
			}
		}

//		System.out.println("1000000��,use:" + (System.currentTimeMillis() - start)+"ms,�ɹ�:"+win+"�Σ�ʧ��:"+lose+"��.");
		// System.Console.ReadKey();
	}
}