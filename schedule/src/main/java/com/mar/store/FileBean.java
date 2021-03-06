package com.mar.store;

import com.mar.model.FileDescriptor;

import java.util.List;

public class FileBean {

	private FileBean parent;
	private List<FileBean> subFiles;
	private FileDescriptor fileDescriptor;
	
	public FileBean(FileDescriptor file){
		this.fileDescriptor=file;
	}
	
	public void addSubFile(FileBean bean){
		if(!subFiles.contains(bean)){
			subFiles.add(bean);
		}
	}

	public List<FileBean> getSubFiles() {
		return subFiles;
	}

	public FileDescriptor getFileDescriptor() {
		return fileDescriptor;
	}

	public FileBean getParent() {
		return parent;
	}

	public void setParent(FileBean parent) {
		this.parent = parent;
	}
}
