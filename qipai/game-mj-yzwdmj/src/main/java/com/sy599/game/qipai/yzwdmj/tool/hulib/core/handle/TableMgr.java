package com.sy599.game.qipai.yzwdmj.tool.hulib.core.handle;

import com.sy599.game.qipai.yzwdmj.tool.hulib.util.HuUtil;

import java.util.List;

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

    public List<Integer> check_ting(int key, int gui_num, boolean eye, boolean chi) {
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

        // ---------------以下为听牌数据表-------------
        for (int i = 0; i < 9; ++i) {
            String path = "ting_table_";
            m_check_ting_table[i].load_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_eye_table_";
            m_check_ting_eye_table[i].load_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_feng_table_";
            m_check_ting_feng_table[i].load_ting(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = "ting_feng_eye_table_";
            m_check_ting_feng_eye_table[i].load_ting(path + i + ".tbl");
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
            String path = HuUtil.source_dir + "feng_table_";
            m_check_feng_table[i].dump(path + i + ".tbl");
        }

        for (int i = 0; i < 9; ++i) {
            String path = HuUtil.source_dir + "feng_eye_table_";
            m_check_feng_eye_table[i].dump(path + i + ".tbl");
        }

        return true;
    }

    public static void main(String args[]) {
        TableMgr.getInstance().load();
    }
}