package com.mar.broadcast.notify;

public interface JobResultNotify {

	void send(String historyId, String data) throws Exception;
}