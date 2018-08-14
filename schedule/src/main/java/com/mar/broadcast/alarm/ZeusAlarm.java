package com.mar.broadcast.alarm;

import com.mar.schedule.mvc.JobFailListener.ChainException;

public interface ZeusAlarm {

	void alarm(String historyId, String title, String content, ChainException chain) throws Exception;

	void alarm(String historyId, String title, String content) throws Exception;
}