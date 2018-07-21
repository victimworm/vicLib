package giaohangtietkiem.vn.printer.Utils.Devices;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class WifiCommunication {

    private static Socket client = null;
    private static OutputStream out = null;
    private static InputStream inStream = null;
    private final Handler mHandler;
    private String AddressIp = null;
    private int port = 0;
    private ConnectThread mConnection = null;
    public static final int WFPRINTER_CONNECTED = 0;
    public static final int WFPRINTER_DISCONNECTED = 1;
    public static final int WFPRINTER_CONNECTEDERR = 2;
    public static final int SEND_FAILED = 4;
    public static final int WFPRINTER_REVMSG = 5;

    public WifiCommunication(Handler handler) {
        this.mHandler = handler;
    }

    public void initSocket(String AddressIp, int port) {
        this.AddressIp = AddressIp;
        this.port = port;
        if (this.mConnection != null) {
            this.mConnection = null;
        }

        if (this.mConnection == null) {
            this.mConnection = new ConnectThread();
            this.mConnection.start();
        }
    }

    public void sendMsg(String sndMsg, String charset) {
        if (sndMsg != null) {
            try {
                byte[] e;
                e = sndMsg.getBytes();
                if (client.isConnected() && !client.isOutputShutdown()) {
                    out.write(e);
                    out.flush();
                }
            } catch (IOException var6) {
                Message msg = this.mHandler.obtainMessage(4);
                this.mHandler.sendMessage(msg);
                Log.d("WIFI-printer", var6.toString());
            }

        }
    }

    public void sndByte(byte[] send) {
        if (send != null) {
            try {
                if (client.isConnected() && !client.isOutputShutdown()) {
                    out.write(send);
                    out.flush();
                }
            } catch (IOException var4) {
                Log.d("WIFI-printer", var4.toString());
                Message msg_ret = this.mHandler.obtainMessage(4);
                this.mHandler.sendMessage(msg_ret);
            }
        }
    }

    public void close() {
        try {
            if (out != null) {
                out.close();
            }
            if (inStream != null) {
                inStream.close();
            }
            if (client != null) {
                client.close();
                out = null;
                inStream = null;
                client = null;
                Message e = this.mHandler.obtainMessage(1);
                this.mHandler.sendMessage(e);
            }
        } catch (IOException var2) {
            Log.d("WIFI-printer", var2.toString());
        }

    }

    public byte[] revMsg() {
        try {
            byte[] e = new byte[1024];
            inStream.read(e);
            return e;
        } catch (Exception var2) {
            Log.d("WIFI-printer", var2.toString());
            return null;
        }
    }

    public int revByte() {
        try {
            return inStream.read();
        } catch (Exception var2) {
            Log.d("WIFI-printer", var2.toString());
            return -1;
        }
    }

    public String bytesToString(byte[] b) {
        String str = null;
        try {
            str = (new String(b, "UTF-8")).trim();
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }
        return str;
    }

    private class ConnectThread extends Thread {
        private ConnectThread() {
        }

        public void run() {
            Message msg_ret;
            try {
                InetAddress e = InetAddress.getByName(WifiCommunication.this.AddressIp);
//                WifiCommunication.client = new Socket(e, WifiCommunication.this.port);
                SocketAddress sockaddr = new InetSocketAddress(e, WifiCommunication.this.port);
                WifiCommunication.client = new Socket();
                WifiCommunication.client.connect(sockaddr, 60000);
                if (WifiCommunication.client != null) {
                    WifiCommunication.out = WifiCommunication.client.getOutputStream();
                    WifiCommunication.inStream = WifiCommunication.client.getInputStream();
                }
                if (WifiCommunication.client != null && WifiCommunication.out != null && WifiCommunication.inStream != null) {
                    msg_ret = WifiCommunication.this.mHandler.obtainMessage(0);
                    WifiCommunication.this.mHandler.sendMessage(msg_ret);
                }
            } catch (IOException var3) {
                msg_ret = WifiCommunication.this.mHandler.obtainMessage(2);
                WifiCommunication.this.mHandler.sendMessage(msg_ret);
            }
        }
    }

    public void closeSocket() {
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkSocketConnect() {
        return client.isClosed();
    }

    public boolean isSocketClosed() {
        return client.isClosed();
    }

}
