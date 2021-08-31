package com.sy599.game.qipai.zjmj.tool.hulib.core.gen_table;

import java.util.Arrays;
import java.util.HashMap;

public class Program {
    static HashMap<Integer, Boolean>[] gui_tested = new HashMap[9];
    static HashMap<Integer, Boolean>[] gui_eye_tested = new HashMap[9];

    static void init_cache() {
        for (int i = 0; i < 9; i++) {
            gui_tested[i] = new HashMap<>();
            gui_eye_tested[i] = new HashMap<>();
        }
    }

    static Boolean check_add(int[] cards, int gui_num, Boolean eye) {
        int key = 0;

        for (int i = 0; i < 9; i++) {
            key = key * 10 + cards[i];
        }

        if (key == 0) {
            return false;
        }

        HashMap<Integer, Boolean> m;
        if (!eye) {
            m = gui_tested[gui_num];
        } else {
            m = gui_eye_tested[gui_num];
        }

        if (m.containsKey(key)) {
            return false;
        }

        m.put(key, true);

        for (int i = 0; i < 9; i++) {
            if (cards[i] > 4) {
                return true;
            }
        }

        TableMgr.getInstance().add(key, gui_num, eye, true);
        return true;
    }

    static void parse_table_sub(int[] cards, int num, boolean eye) {
        for (int i = 0; i < 9; i++) {
            if (cards[i] == 0) {
                continue;
            }

            cards[i]--;

            if (!check_add(cards, num, eye)) {
                cards[i]++;
                continue;
            }

            if (num < 8) {
                parse_table_sub(cards, num + 1, eye);
            }

            cards[i]++;
        }
    }

    static void parse_table(int[] cards, boolean eye) {

        if (!check_add(cards, 0, eye)) {
            return;
        }

        parse_table_sub(cards, 1, eye);
    }

    /**
     * @param cards ����
     * @param level �㼶(��1��ʼ)
     * @param eye   �Ƿ��н�
     */
    static void gen_auto_table_sub(int[] cards, int level, boolean eye) {
        for (int i = 0; i < 16; ++i) {
            if (i <= 8) {
                if (cards[i] > 3) {
                    continue;
                }
                cards[i] += 3;
            } else {
                int index = i - 9;
                if (cards[index] > 5 || cards[index + 1] > 5 || cards[index + 2] > 5) {
                    continue;
                }
                cards[index] += 1;
                cards[index + 1] += 1;
                cards[index + 2] += 1;
            }

            parse_table(cards, eye);

            if (level < 4) {
                gen_auto_table_sub(cards, level + 1, eye);
            }

            if (i <= 8) {
                cards[i] -= 3;
            } else {
                int index = i - 9;
                cards[index] -= 1;
                cards[index + 1] -= 1;
                cards[index + 2] -= 1;
            }
        }
    }

    static void gen_table() {
        int[] cards = new int[34];
        Arrays.fill(cards, 0);

        gen_auto_table_sub(cards, 1, false);
    }

    static void gen_eye_table() {
        int[] cards = new int[34];
        Arrays.fill(cards, 0);

        for (int i = 0; i < 9; ++i) {
            cards[i] = 2;
            parse_table(cards, true);
            gen_auto_table_sub(cards, 1, true);
            cards[i] = 0;
        }
    }

    public static void main(String[] args) {
//		System.out.println("generate table begin...");
        init_cache();
        gen_table();
        gen_eye_table();
        TableMgr.getInstance().dump_table();
    }
}