package com.sy.sanguo.common.exception;

public class GameException extends Exception{
	private static final long serialVersionUID = 1L;
	
	/**
	 * 错误号
	 */
	private int errNum;
	
	/**
	 * 具体异常
	 */
	private Throwable e;
	
	public GameException(int errNum){
		this(errNum,null);
	}
	
	public GameException(int errNum,Throwable e){
		this.errNum = errNum;
		this.e = e;
	}
	
	public int getErrNum() {
		return errNum;
	}

	public Throwable getE() {
		return e;
	}

	@Override
	public String getLocalizedMessage() {
		return this.errNum+"";
	}

	
	
	
}
