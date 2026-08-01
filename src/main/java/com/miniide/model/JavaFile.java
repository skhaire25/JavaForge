package com.miniide.model;

public class JavaFile {
	private int fileId;
	private String fileName;
	private String sourceCode;
	private int packageId;
	private boolean hasError;

	
	public JavaFile() {
		super();
	}

	public JavaFile(int fileId, String fileName, String sourceCode, int packageId) {
		super();
		this.fileId = fileId;
		this.fileName = fileName;
		this.sourceCode = sourceCode;
		this.packageId = packageId;
	}

	@Override
	public String toString() {
		return "\nFile ID: " + fileId +"\nFile Name: " + fileName +"\nPackage ID: " + packageId+"\n";
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public int getPackageId() {
		return packageId;
	}

	public void setPackageId(int packageId) {
		this.packageId = packageId;
	}
	
	public boolean isHasError() {
	    return hasError;
	}
	
	public void setHasError(boolean hasError) {
	    this.hasError = hasError;
	}

}
