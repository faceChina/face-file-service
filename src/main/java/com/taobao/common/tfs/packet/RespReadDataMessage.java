/*
 * (C) 2007-2010 Alibaba Group Holding Limited.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.
 *
 */
package com.taobao.common.tfs.packet;

import com.taobao.common.tfs.etc.TfsConstant;

public class RespReadDataMessage extends BasePacket {

    protected int length;
    protected byte[] data;

    public RespReadDataMessage(Transcoder transcoder) {
        super(transcoder);
        this.pcode = TfsConstant.RESP_READ_DATA_MESSAGE;
    }

    @Override
    public boolean decode() {
        length = byteBuffer.getInt();
        if (length > 0)
            data = StreamTranscoderUtil.readByteArray(byteBuffer, length);
        return true;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


}
