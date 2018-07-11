package io.openmessaging;

import io.openmessaging.utils.UnsafeUtil;
import sun.misc.Unsafe;
import sun.nio.ch.DirectBuffer;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import static io.openmessaging.config.MessageStoreConfig.MESSAGE_SIZE;
import static io.openmessaging.utils.UnsafeUtil.UNSAFE;


/**
 * Created by IntelliJ IDEA.
 * User: yangyuming
 * Date: 2018/7/6
 * Time: 下午8:15
 */
class DirectQueueCache {

    private ByteBuffer byteBuffer;

    private final long address;

    private byte size = 0;

    private int offset = -1;

    DirectQueueCache(int cacheSize) {
        this.byteBuffer = ByteBuffer.allocateDirect(cacheSize * MESSAGE_SIZE);
        this.address = ((DirectBuffer) byteBuffer).address();
    }


    int addMessage(byte[] msg) {
        UNSAFE.putByte(address + size * MESSAGE_SIZE, (byte) msg.length);
        for (int i = 0; i < msg.length; i++) {
            UNSAFE.putByte(address + size * MESSAGE_SIZE + i + 1, msg[i]);
        }

//        byteBuffer.put((byte) msg.length);
//        byteBuffer.put(msg);
        byteBuffer.position(++size * MESSAGE_SIZE);
        return size;
    }

    ByteBuffer getByteBuffer() {
        /*limit设为当前position,position设为0*/
        return byteBuffer;
    }

    void putTerminator() {
        byteBuffer.put((byte) 0);
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public byte getSize() {
        return size;
    }

    void clear() {
        /*position设为0，limit设为capacity，回到写模式*/
        byteBuffer.clear();
        size = 0;
    }

    /*ReadQueueCache*/

    ArrayList<byte[]> getMessage(int start, int end) {

        ArrayList<byte[]> msgList = new ArrayList<>();

//        for (int i = start; i < end; i++) {
//            byteBuffer.position(i * MESSAGE_SIZE);
//            byte size = byteBuffer.get();
//            if (size == 0) break;
//            byte[] msg = new byte[size];
//            byteBuffer.get(msg, 0, size);
//            msgList.add(msg);
//        }

        /** Unsafe **/

        for (int i = start; i < end; i++) {
            long pos = address + i * MESSAGE_SIZE;
            byte size = UNSAFE.getByte(pos);
            if (size == 0) break;
            byte[] msg = new byte[size];
            for (int j = 0; j < size; j++) {
                msg[j] = UNSAFE.getByte(pos + j + 1);
            }
            msgList.add(msg);
        }

        return msgList;
    }

    public ByteBuffer getWriteBuffer() {
        byteBuffer.clear();
        return byteBuffer;
    }
}
