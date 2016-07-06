package com.zjlp.face.file.file;


public class FileStatus{
	/** 文件ID */
	private long id;
	/** 文件在块中的位置*/
	private int offset;
	/** 文件的长度,支持大文件 */
	private long length; 
	/** 文件在块中占据的长度,支持大文件 */
	private long occupyLength;
	/** 文件修改时间 */							
	private int modifyTime; 
	/** 文件创建时间 */
	private int createTime; //创建时间
	/** 文件状态 ( 0 正常 1隐藏  2 删除) */
	private int flag; 
	
	private int crc; // crc checksum
	
	public FileStatus() {
	}

	public FileStatus(com.taobao.common.tfs.packet.FileInfo fileInfo) {
		this.id = fileInfo.getId();
		this.offset = fileInfo.getOffset();
		this.length = fileInfo.getLength();
		this.occupyLength = fileInfo.getOccupyLength();
		this.createTime = fileInfo.getCreateTime();
		this.modifyTime = fileInfo.getModifyTime();
		this.flag = fileInfo.getFlag();
		this.crc = fileInfo.getCrc();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public long getLength() {
		return length;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public long getOccupyLength() {
		return occupyLength;
	}

	public void setOccupyLength(long occupyLength) {
		this.occupyLength = occupyLength;
	}

	public int getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(int modifyTime) {
		this.modifyTime = modifyTime;
	}

	public int getCreateTime() {
		return createTime;
	}

	public void setCreateTime(int createTime) {
		this.createTime = createTime;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getCrc() {
		return crc;
	}

	public void setCrc(int crc) {
		this.crc = crc;
	}

}
