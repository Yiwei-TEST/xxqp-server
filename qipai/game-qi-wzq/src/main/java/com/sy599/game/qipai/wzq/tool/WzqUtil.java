package com.sy599.game.qipai.wzq.tool;

public class WzqUtil {

    /**
     * 坐标是否合法
     *
     * @param x
     * @param y
     * @return
     */
    public static boolean isValid(int x, int y) {
        return x >= 0 && x < 15 && y >= 0 && y < 15;
    }

    /**
     * 坐标是否可以走棋
     *
     * @param qiPan
     * @param x
     * @param y
     * @return
     */
    public static boolean canPlay(int[][] qiPan, int x, int y) {
        return qiPan[x][y] == 0;
    }

    /**
     * 是否可以结束
     * <p>
     * 八个方向去找是否有五个一样的颜色，
     * 竖直、水平、左斜、右斜
     *
     * @param qiPan 棋盘
     * @param x     最后一个走棋的x坐标
     * @param y     最后一个走棋的y坐标
     * @return
     */
    public static boolean isWin(int[][] qiPan, int x, int y) {
        int val = qiPan[x][y];
        if (val == 0) {
            return false;
        }
        int count = 0;

        // 竖直 向上
        for (int i = y; i < 15; i++) {
            if (qiPan[x][i] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }
        // 竖直 向下
        for (int i = y - 1; i >= 0; i--) {
            if (qiPan[x][i] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }

        // 重置计数
        count = 0;

        // 水平 向右
        for (int i = x; i < 15; i++) {
            if (qiPan[i][y] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }
        // 水平 向左
        for (int i = x - 1; i >= 0; i--) {
            if (qiPan[i][y] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }

        // 重置计数
        count = 0;

        // 左斜 右上
        for (int i = x, j = y; i < 15 && j < 15; i++, j++) {
            if (qiPan[i][j] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }
        // 左斜 左下
        for (int i = x - 1, j = y - 1; i >= 0 && j >= 0; i--, j--) {
            if (qiPan[i][j] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }

        // 重置计数
        count = 0;

        // 右斜 左上
        for (int i = x, j = y; i >= 0 && j < 15; i--, j++) {
            if (qiPan[i][j] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }
        // 右斜 右下
        for (int i = x + 1, j = y - 1; i < 15 && j >= 0; i++, j--) {
            if (qiPan[i][j] == val) {
                count++;
                if (count == 5) {
                    return true;
                }
            } else {
                break;
            }
        }
        return false;
    }

    /**
     * 棋盘是否满了
     *
     * @param qiPan
     * @return
     */
    public static boolean isFull(int[][] qiPan) {
        for (int x = 0; x < 15; x++) {
            for (int y = 0; y < 15; y++) {
                if (qiPan[x][y] == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String[] args) {

        String qiPanStr = "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,-1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0";
        int color = -1;
        int x = 7;
        int y = 7;
        int[][] qiPan = new int[15][15];
        String[] splits = qiPanStr.split(",");
        for (int i = 0; i < splits.length; i++) {
            int xt = i / 15;
            int yt = i % 15;
            qiPan[xt][yt] = Integer.valueOf(splits[i]);
            if (qiPan[xt][yt] == color) {
                x = xt;
                y = yt;
            }
        }
        boolean win = isWin(qiPan, x, y);
        System.out.println("isWin = " + win);
    }


}
