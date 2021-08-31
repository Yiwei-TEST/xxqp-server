package com.sy599.game.qipai.dtz.bean;

public class BombScore {

	private int tongZi_K = 100;
	private int tongZi_A = 200;
	private int tongZi_2 = 300;
	private int tongZi_Wang = 400;
	private int diBomb = 400;
	
	private int xi = 100;
	private int wang_Xi = 200;

	private int Xi_5Q = 400;
	private int Xi_K = 500;
	private int Xi_A = 600;
	private int Xi_2 = 700;

	public void load(int wangTongZi){
		if(wangTongZi == 1){
			setDiBomb(500);
		}else{
			setDiBomb(400);
		}
	}
	
	public int getTongZi_K() {
		return tongZi_K;
	}
	public void setTongZi_K(int tongZi_K) {
		this.tongZi_K = tongZi_K;
	}
	public int getTongZi_A() {
		return tongZi_A;
	}
	public void setTongZi_A(int tongZi_A) {
		this.tongZi_A = tongZi_A;
	}
	public int getTongZi_2() {
		return tongZi_2;
	}
	public void setTongZi_2(int tongZi_2) {
		this.tongZi_2 = tongZi_2;
	}
	public int getTongZi_Wang() {
		return tongZi_Wang;
	}
	public void setTongZi_Wang(int tongZi_Wang) {
		this.tongZi_Wang = tongZi_Wang;
	}
	public int getDiBomb() {
		return diBomb;
	}
	public void setDiBomb(int diBomb) {
		this.diBomb = diBomb;
	}
	public int getXi() {
		return xi;
	}
	public void setXi(int xi) {
		this.xi = xi;
	}
	public int getWang_Xi() {
		return wang_Xi;
	}
	public void setWang_Xi(int wang_Xi) {
		this.wang_Xi = wang_Xi;
	}

    public int getXi_5Q() {
        return Xi_5Q;
    }

    public void setXi_5Q(int xi_5Q) {
        Xi_5Q = xi_5Q;
    }

    public int getXi_K() {
        return Xi_K;
    }

    public void setXi_K(int xi_K) {
        Xi_K = xi_K;
    }

    public int getXi_A() {
        return Xi_A;
    }

    public void setXi_A(int xi_A) {
        Xi_A = xi_A;
    }

    public int getXi_2() {
        return Xi_2;
    }

    public void setXi_2(int xi_2) {
        Xi_2 = xi_2;
    }
}
