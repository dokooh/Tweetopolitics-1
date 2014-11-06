/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package listener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author filippia
 */
public class Communication {
    private BufferedInputStream in;
    private BufferedOutputStream out;

    public Communication(BufferedInputStream in, BufferedOutputStream out) {
        this.in = in;
        this.out = out;
    }
    
     public String SendMessage(String msg) {
        try {
            byte[] utf8Bytes = msg.getBytes("UTF-8");
            int msglen = utf8Bytes.length; // prints "11"
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(msglen);
            byte[] toClientLen = b.array();
            out.write(toClientLen, 0, toClientLen.length);
            out.flush();
            byte toClientMsg[] = new byte[msglen];
            toClientMsg = msg.getBytes();
            out.write(toClientMsg, 0, toClientMsg.length);
            out.flush();
            return "SUCCESS";
        } catch (IOException ioException) {
            System.out.println("Unable to send message on server" + ioException.toString());
            return "ERROR";
        }
    }

    public String ReceiveMessage() {
        int msglen, n;
        try {
            byte[] fromClientLen = new byte[4];
            n = in.read(fromClientLen, 0, 4);
            ByteBuffer b = ByteBuffer.wrap(fromClientLen); 
            b.order(ByteOrder.LITTLE_ENDIAN);
            msglen = b.getInt();
        } catch (IOException ioException) {
            System.out.println("Unable to receive message on server" + ioException.toString());
            return "ERROR";
        }
        try {
            byte[] fromClientMsg = new byte[msglen];
            n = in.read(fromClientMsg, 0, msglen);
            //System.out.println("received: "+ new String(fromClientMsg));
            return (new String(fromClientMsg));
        } catch (IOException ioException) {
            System.out.println("Unable to receive message on server" + ioException.toString());
            return "ERROR";
        }
    }
}
