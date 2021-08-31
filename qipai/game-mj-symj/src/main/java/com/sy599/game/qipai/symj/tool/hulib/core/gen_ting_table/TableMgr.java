package com.sy599.game.qipai.symj.tool.hulib.core.gen_ting_table;

import java.util.Map;

public class TableMgr {

    public SetTable[] m_check_table = new SetTable[9];
    public SetTable[] m_check_eye_table = new SetTable[9];
    public SetTable[] m_check_feng_table = new SetTable[9];
    public SetTable[] m_check_feng_eye_table = new SetTable[9];

    public SetTable[] m_check_ting_table = new SetTable[9];
    public SetTable[] m_check_ting_eye_table = new SetTable[9];
    public SetTable[] m_check_ting_feng_table = new SetTable[9];
    public SetTable[] m_check_ting_feng_eye_table = new SetTable[9];

    public static TableMgr mgr = new TableMgr();

    public static TableMgr getInstance() {
        return mgr;
    }

    public TableMgr() {
        for (int i = 0; i < 9; ++i) {
            m_check_table[i] = new SetTable();
            m_check_eye_table[i] = new SetTable();
            m_check_feng_table[i] = new SetTable();
            m_check_feng_eye_table[i] = new SetTable();

            m_check_ting_table[i] = new SetTable();
            m_check_ting_eye_table[i] = new SetTable();
            m_check_ting_feng_table[i] = new SetTable();
            m_check_ting_feng_eye_table[i] = new SetTable();
        }
    }

    public boolean check_ting(int key, int gui_num, boolean eye, boolean chi) {
        SetTable tbl;

        if (chi) {
            if (eye) {
                tbl = m_check_ting_eye_table[gui_num];
            } else {
                tbl = m_check_ting_table[gui_num];
            }
        } else {
            if (eye) {
                tbl = m_check_ting_feng_eye_table[gui_num];
            } else {
                tbl = m_check_ting_feng_table[gui_num];
            }
        }

        return tbl.check_ting(key);
    }

    public boolean check(int key, int gui_num, boolean eye, boolean chi) {
        SetTable tbl;

        if (chi) {
            if (eye) {
                tbl = m_check_eye_table[gui_num];
            } else {
                tbl = m_check_table[gui_num];
            }
        } else {
            if (eye) {
                tbl = m_check_feng_eye_table[gui_num];
            } else {
                tbl = m_check_feng_table[gui_num];
            }
        }

        return tbl.check(key);
    }

    public void add(int key, int gui_num, boolean eye, boolean chi) {
        SetTable tbl;

        if (chi) {
            if (eye) {
                tbl = m_check_eye_table[gui_num];
            } else {
                tbl = m_check_table[gui_num];
            }
        } else {
            if (eye) {
                tbl = m_check_feng_eye_table[gui_num];
            } else {
                tbl = m_check_feng_table[gui_num];
            }
        }

        tbl.add(key);
    }

    public void gen_ting_table() {
        gen_ting_table_sub(9, m_check_table, m_check_ting_table);
        gen_ting_table_sub(9, m_check_eye_table, m_check_ting_eye_table);
        gen_ting_table_sub(7, m_check_feng_table, m_check_ting_feng_table);
        gen_ting_table_sub(7, m_check_feng_eye_table, m_check_ting_feng_eye_table);
    }

    public void gen_ting_table_sub(int length, SetTable[] noTingTbl, SetTable[] tingTbl) {
        int index = 0;
        String tingKey = null;
        String numberStr = null;
        SetTable setTable = null;
        SetTable setTingTable = null;
        StringBuilder strBuilder = null;
        Map<Integer, Boolean> noTingMap = null;

        for (int i = 0; i < 9; ++i) {
            setTable = noTingTbl[i];
            setTingTable = tingTbl[i];
            noTingMap = setTable.getM_tbl();

            for (int number : noTingMap.keySet()) {
                numberStr = String.valueOf(number);
                for (int j = 0; j < numberStr.length(); j++) {
                    if (!"0".equals(String.valueOf(numberStr.charAt(j)))) {
                        strBuilder = new StringBuilder(numberStr);
                        strBuilder.setCharAt(j, (char) (numberStr.charAt(j) - 1));
                        tingKey = strBuilder.toString();
                        index = length - numberStr.length() + j;
                        setTingTable.add_ting(Integer.parseInt(tingKey), index);
                    }
                }
            }
        }
    }


    public boolean load() {
        for (int i = 0; i < 9; ++i) {
            String path = "table_";
            m_check_table[i].load(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "eye_table_";
            m_check_eye_table[i].load(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "feng_table_";
            m_check_feng_table[i].load(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "feng_eye_table_";
            m_check_feng_eye_table[i].load(path + i + ".tbl");
        }

        return true;
    }

    // 生成所有听牌文件
    public boolean dump_ting_table() {

        for (int i = 0; i < 9; ++i) {
            String path = "ting_table_";
            m_check_ting_table[i].dump_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_eye_table_";
            m_check_ting_eye_table[i].dump_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_feng_table_";
            m_check_ting_feng_table[i].dump_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_feng_eye_table_";
            m_check_ting_feng_eye_table[i].dump_ting(path + i + ".tbl");
        }

        return true;
    }

    public boolean dump_table() {
        for (int i = 0; i < 9; ++i) {
            String path = "table_";
            m_check_table[i].dump(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "eye_table_";
            m_check_eye_table[i].dump(path + i + ".tbl");
        }

        return true;
    }

    public boolean dump_feng_table() {
        for (int i = 0; i < 9; ++i) {
            String path = "feng_table_";
            m_check_feng_table[i].dump(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "feng_eye_table_";
            m_check_feng_eye_table[i].dump(path + i + ".tbl");
        }

        return true;
    }
}