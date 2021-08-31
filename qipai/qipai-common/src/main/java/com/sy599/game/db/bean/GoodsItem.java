package com.sy599.game.db.bean;

/**
 * 商品项
 */
public class GoodsItem {

    /*** 类型：钻石兑换金币***/
    public static final int type_cards_2_golds = 1;
    /*** 类型：金币兑换金币***/
    public static final int type_goods_2_cards = 2;

    private int id;//id
    private int type;//类型
    private String name;//商品名称
    private String desc;//描述
    private int discount;//折扣（/100）
    private long amount;//价值
    private long count;//兑换数量
    private long give;//赠送数量
    private int ratio;//倍率（/100）

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public long getGive() {
        return give;
    }

    public void setGive(long give) {
        this.give = give;
    }

    public int getRatio() {
        return ratio;
    }

    public void setRatio(int ratio) {
        this.ratio = ratio;
    }
}
