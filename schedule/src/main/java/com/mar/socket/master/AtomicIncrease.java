package com.mar.socket.master;

import java.util.concurrent.atomic.AtomicInteger;

public class AtomicIncrease {

	private static AtomicInteger rid=new AtomicInteger();
	
	public static int getAndIncrement(){
		return rid.getAndIncrement();
	}
}
