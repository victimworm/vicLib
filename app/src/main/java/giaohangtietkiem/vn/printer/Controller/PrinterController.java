package giaohangtietkiem.vn.printer.Controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

import giaohangtietkiem.vn.printer.Interfaces.IFSimpleCallbackController;
import giaohangtietkiem.vn.printer.R;
import giaohangtietkiem.vn.printer.Utils.Bitmap.BitmapLayout;
import giaohangtietkiem.vn.printer.Utils.Bitmap.BitmapLayoutComponent;
import giaohangtietkiem.vn.printer.Utils.Constant;
import giaohangtietkiem.vn.printer.Utils.Devices.DeviceConstant;
import giaohangtietkiem.vn.printer.Utils.Devices.SendDataToPrintKeepConnThread;
import giaohangtietkiem.vn.printer.Utils.Devices.SendDataToPrinterThread;
import giaohangtietkiem.vn.printer.Utils.Devices.WifiCommunication;

public class PrinterController {

    private static int PRINTER_PORT = 9100;
    private Context context;
    private static final int WFPRINTER_REVMSG = 5;

    private WifiCommunication wifiCommunication = null;
    private DeviceConstant.PrinterStatus printerStatus;

    public static PrinterController instance(Context context) {
        PrinterController controller = new PrinterController();
        controller.context = context;
        return controller;
    }

    @SuppressLint("HandlerLeak")
    public void checkPrinterConnection(String ip, final IFSimpleCallbackController callback) {
        wifiCommunication = new WifiCommunication(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiCommunication.WFPRINTER_CONNECTED: {
                        wifiCommunication.closeSocket();
                        callback.onSuccess("Connect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_DISCONNECTED: {
                        callback.onSuccess("Disconnect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.SEND_FAILED: {
                        callback.onFailure("Send Data Failed,please reconnect");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_CONNECTEDERR: {
                        callback.onFailure("Connect the WIFI-printer error");
                        break;
                    }
                    case WFPRINTER_REVMSG: {
                        byte revData = (byte) Integer.parseInt(msg.obj.toString());
                        if (((revData >> 6) & 0x01) == 0x01) {
                            callback.onFailure("The printer has no paper");
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        wifiCommunication.initSocket(ip, PRINTER_PORT);
    }

    @SuppressLint("HandlerLeak")
    public void printPackageList(final ArrayList<Package> pkgs, final IFSimpleCallbackController callback) {
        wifiCommunication = new WifiCommunication(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiCommunication.WFPRINTER_CONNECTED: {
                        SendDataToPrintKeepConnThread printDataThread = new SendDataToPrintKeepConnThread(pkgs, printerStatus, context);
                        printDataThread.wfComm = wifiCommunication;
                        printDataThread.start();
                        callback.onSuccess("Connect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_DISCONNECTED: {
                        callback.onSuccess("Disconnect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.SEND_FAILED: {
                        callback.onFailure("Send Data Failed,please reconnect");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_CONNECTEDERR: {
                        callback.onFailure("Connect the WIFI-printer error");
                        break;
                    }
                    case WFPRINTER_REVMSG: {
                        byte revData = (byte) Integer.parseInt(msg.obj.toString());
                        if (((revData >> 6) & 0x01) == 0x01) {
                            callback.onFailure("The printer has no paper");
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        wifiCommunication.initSocket(Constant.printerIp, PRINTER_PORT);
    }

    @SuppressLint("HandlerLeak")
    public void printImage(final Bitmap bm, final IFSimpleCallbackController callback) {
        wifiCommunication = new WifiCommunication(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiCommunication.WFPRINTER_CONNECTED: {
                        callback.onSuccess("Connect the WIFI-printer successful");
                        if (!wifiCommunication.isSocketClosed()) {
                            SendDataToPrinterThread printDataThread = new SendDataToPrinterThread(bm, context, printerStatus);
                            printDataThread.wfComm = wifiCommunication;
                            printDataThread.start();
                        }
                        break;
                    }
                    case WifiCommunication.WFPRINTER_DISCONNECTED: {
                        callback.onSuccess("Disconnect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.SEND_FAILED: {
                        callback.onFailure("Send Data Failed,please reconnect");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_CONNECTEDERR: {
                        callback.onFailure("Connect the WIFI-printer error");
                        break;
                    }
                    case WFPRINTER_REVMSG: {
                        byte revData = (byte) Integer.parseInt(msg.obj.toString());
                        if (((revData >> 6) & 0x01) == 0x01) {
                            callback.onFailure("The printer has no paper");
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        wifiCommunication.initSocket(Constant.printerIp, PRINTER_PORT);
    }

    @SuppressLint("HandlerLeak")
    public void printPackage(final Package pkg, final IFSimpleCallbackController callback) {
        wifiCommunication = new WifiCommunication(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case WifiCommunication.WFPRINTER_CONNECTED: {
                        callback.onSuccess("Connect the WIFI-printer successful");
                        if (!wifiCommunication.isSocketClosed()) {
                            Bitmap bm = generateBitmap(pkg);
                            SendDataToPrinterThread printDataThread = new SendDataToPrinterThread(bm, context, printerStatus);
                            printDataThread.wfComm = wifiCommunication;
                            printDataThread.start();
                        }
                        break;
                    }
                    case WifiCommunication.WFPRINTER_DISCONNECTED: {
                        callback.onSuccess("Disconnect the WIFI-printer successful");
                        break;
                    }
                    case WifiCommunication.SEND_FAILED: {
                        callback.onFailure("Send Data Failed,please reconnect");
                        break;
                    }
                    case WifiCommunication.WFPRINTER_CONNECTEDERR: {
                        callback.onFailure("Connect the WIFI-printer error");
                        break;
                    }
                    case WFPRINTER_REVMSG: {
                        byte revData = (byte) Integer.parseInt(msg.obj.toString());
                        if (((revData >> 6) & 0x01) == 0x01) {
                            callback.onFailure("The printer has no paper");
                        }
                        break;
                    }
                    default: {
                        break;
                    }
                }
            }
        });

        wifiCommunication.initSocket(Constant.printerIp, PRINTER_PORT);
    }

    public Bitmap generateBitmap() {
        Bitmap bitmap = null;
        BitmapLayout bitMapLayout = new BitmapLayout(context, 380, 385);
        bitMapLayout.setAutoVertical(true);
//        bitMapLayout.setPadding(3);
//        bitMapLayout.setBorderRadius(5);

        bitMapLayout.addComponent(BitmapLayout.COMPONENT_IMAGE_CONTENT, new BitmapLayoutComponent(R.drawable.swing, 10, 10, 233, 308, true, true));

        bitmap = bitMapLayout.createBitmapLayout();
        return bitmap;
    }

    private Bitmap generateBitmap(Package pkg) {
        Bitmap bitmap = null;
        BitmapLayout bitMapLayout = new BitmapLayout(context, 380, 385);
        bitMapLayout.setAutoVertical(true);
        bitMapLayout.setPadding(3);
        bitMapLayout.setBorderRadius(5);

//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_IMAGE_CONTENT, new BitmapLayoutComponent(R.drawable.logo_app_login_update, 10, 10, 256, 40, true, false));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BARCODE_CONTENT, new BitmapLayoutComponent(pkg.order, 0, 0, 360, 85, false, true));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BOLD_TEXT_CONTENT, new BitmapLayoutComponent(pkg.order, 40, 0, 0, false, true, 0, 3));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_LINE_CONTENT, new BitmapLayoutComponent(4, 0, 375, 0, -15, 3));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BOLD_TEXT_CONTENT, new BitmapLayoutComponent(pkg.deliver_cart_alias, 35, 10, 0, false, true, 25, 25));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_LINE_CONTENT, new BitmapLayoutComponent(4, 0, 375, 0, -5, 20));
//        String clientId = pkg.client_id.equals("") ? "" : "/" + pkg.client_id;
//        String created_username = pkg.created_username;
//        if (created_username.equals("")) {
//            created_username = pkg.shop_name;
//        } else {
//            if (created_username.contains(pkg.shop_code + " - ")) {
//                created_username = created_username.replace(pkg.shop_code + " - ", "");
//            }
//        }
//
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(created_username + clientId, 20, 10, 0, false, false, 1));
//        String customerInfo = pkg.customer_fullname + ", " + pkg.customer_tel + ", " + pkg.customer_first_address + ", " + pkg.customer_last_address;
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(customerInfo, 20, 10, 0, false, false));
//        if (pkg.productList.size() > 0) {
//            String productsName = pkg.productList.get(0).product_name;
//            bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(productsName, 20, 10, 0, false, false));
//        }

        bitmap = bitMapLayout.createBitmapLayout();

        return bitmap;
    }

}
