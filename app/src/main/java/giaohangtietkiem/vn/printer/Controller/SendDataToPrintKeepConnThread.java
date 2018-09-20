package giaohangtietkiem.vn.printer.Controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterTSC;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import giaohangtietkiem.vn.printer.Objects.Package;
import giaohangtietkiem.vn.printer.R;
import giaohangtietkiem.vn.printer.Utils.Bitmap.BitmapLayout;
import giaohangtietkiem.vn.printer.Utils.Bitmap.BitmapLayoutComponent;
import giaohangtietkiem.vn.printer.Utils.Devices.DeviceConstant;
import giaohangtietkiem.vn.printer.Utils.Devices.WifiCommunication;

public class SendDataToPrintKeepConnThread extends Thread {

    public WifiCommunication wfComm = null;
    public DeviceConstant.PrinterStatus printerStatus;
    private ArrayList<Package> packageList = new ArrayList<>();
    private Context context;

    public SendDataToPrintKeepConnThread(ArrayList<Package> bitmaps, DeviceConstant.PrinterStatus printerStatus, Context context) {
        this.packageList.addAll(bitmaps);
        this.printerStatus = printerStatus;
        this.context = context;
    }

    @Override
    public void run() {
        super.run();

        ArrayList<String> pkgIds = new ArrayList<>();
        ArrayList<String> aliasListPrintSuccess = new ArrayList<>();

        for (int i = 0; i < packageList.size(); i++) {
            Bitmap mBitmap = generateBitmap(packageList.get(i));
            if (mBitmap != null) {
                try {
                    if (wfComm != null) {
                        wfComm.sndByte(DataForSendToPrinterTSC.sizeBymm(75, 50));
                        wfComm.sndByte(DataForSendToPrinterTSC.gapBymm(2, 0));
                        wfComm.sndByte(DataForSendToPrinterTSC.speed(5));
                        wfComm.sndByte(DataForSendToPrinterTSC.direction(1));
                        wfComm.sndByte(DataForSendToPrinterTSC.cls());
                        byte[] data1 = bitmap(5, 0, 0, mBitmap, BitmapToByteData.BmpType.Threshold);
                        wfComm.sndByte(data1);
                        wfComm.sndByte(DataForSendToPrinterTSC.print(1));

                        aliasListPrintSuccess.add(packageList.get(i).alias);
                    }

                    pkgIds.add(packageList.get(i).id);
                } catch (Exception e) {
                    aliasListPrintSuccess.remove(aliasListPrintSuccess.size() - 1);
                    if (wfComm != null) {
                        wfComm.closeSocket();
                    }
                }
            }
        }

        wfComm.closeSocket();
    }

    public Bitmap generateBitmap(Package pkg) {
        Bitmap bitmap = null;
        BitmapLayout bitMapLayout = new BitmapLayout(context, 380, 385);
        bitMapLayout.setAutoVertical(true);
        bitMapLayout.setPadding(3);
        bitMapLayout.setBorderRadius(5);

        bitMapLayout.addComponent(BitmapLayout.COMPONENT_IMAGE_CONTENT, new BitmapLayoutComponent(R.drawable.logo_app_login_update, 10, 10, 256, 40, true, false));
        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BARCODE_CONTENT, new BitmapLayoutComponent(pkg.order, 0, 0, 360, 85, false, true));
        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BOLD_TEXT_CONTENT, new BitmapLayoutComponent(pkg.order, 40, 0, 0, false, true, 0, 3));
        bitMapLayout.addComponent(BitmapLayout.COMPONENT_LINE_CONTENT, new BitmapLayoutComponent(4, 0, 375, 0, -15, 3));
//        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BOLD_TEXT_CONTENT, new BitmapLayoutComponent(pkg.deliver_cart_alias, 35, 10, 0, false, true, 25, 25));

//        if (pkg.alias.contains("Bộ") || pkg.alias.contains("bộ") || pkg.alias.contains("BO") || pkg.alias.contains("bo")) {
//            bitMapLayout.addComponent(BitmapLayout.COMPONENT_HORIZONTAL_COMBINE, new BitmapLayoutComponent(R.drawable.label_truck, "BO." + pkg.deliver_cart_alias, 35, true, 10, 0, 42, 42, true));
//        } else {
        bitMapLayout.addComponent(BitmapLayout.COMPONENT_BOLD_TEXT_CONTENT, new BitmapLayoutComponent(pkg.deliver_cart_alias, 35, 10, 0, false, true, 25, 25));
//        }

        bitMapLayout.addComponent(BitmapLayout.COMPONENT_LINE_CONTENT, new BitmapLayoutComponent(4, 0, 375, 0, -5, 20));
        String clientId = pkg.client_id.equals("") ? "" : "/" + pkg.client_id;

        String created_username = pkg.shop_name;
        if (created_username.equals("")) {
            if (pkg.created_username.equals("") || pkg.created_username.equals("0")) {
                created_username = pkg.shop_code;
            } else {
                created_username = pkg.created_username;
                if (created_username.contains(pkg.shop_code + " - ")) {
                    created_username = created_username.replace(pkg.shop_code + " - ", "");
                }
            }
        }

        bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(created_username + clientId, 20, 10, 0, false, false, 1));
        String customerInfo = pkg.customer_fullname + ", " + pkg.customer_tel + ", " + pkg.customer_first_address + ", " + pkg.customer_last_address;
        bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(customerInfo, 20, 10, 0, false, false));
        if (pkg.productList.size() > 0) {
            String productsName = pkg.productList.get(0);
            bitMapLayout.addComponent(BitmapLayout.COMPONENT_TEXT_CONTENT, new BitmapLayoutComponent(productsName, 20, 10, 0, false, false));
        }

        bitmap = bitMapLayout.createBitmapLayout();

        return bitmap;
    }

    public byte[] bitmap(int x, int y, int mode, Bitmap bitmap, BitmapToByteData.BmpType bmpType) {
        int width = (bitmap.getWidth() + 7) / 8;
        int heigth = bitmap.getHeight();
        String str = "BITMAP " + x + "," + y + "," + width + "," + heigth + "," + mode + ",";
        String end = "\n";
        byte[] ended = strTobytes(end);
        byte[] head = strTobytes(str);
        byte[] data = downLoadBmpToSendTSCData(bitmap, bmpType);
        data = byteMerger(head, data);
        data = byteMerger(data, ended);
        return data;
    }

    private byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private byte[] strTobytes(String str) {
        Object b = null;
        byte[] data = null;

        try {
            byte[] b1 = str.getBytes("utf-8");
            data = (new String(b1, "utf-8")).getBytes("gbk");
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
        }

        return data;
    }

    public byte[] downLoadBmpToSendTSCData(Bitmap mBitmap, BitmapToByteData.BmpType bmpType) {
        Bitmap bitmap = toGrayscale(mBitmap);
        bitmap = convertGreyImg(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] data = getbmpdataTsc(pixels, width, height);
        return data;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    public Bitmap convertGreyImg(Bitmap img) {
        int width = img.getWidth();
        int height = img.getHeight();
        int[] pixels = new int[width * height];
        img.getPixels(pixels, 0, width, 0, 0, width, height);
        double redSum = 0.0D;
        double total = (double) (width * height);

        int m;
        int mBitmap;
        int j;
        int grey;
        for (m = 0; m < height; ++m) {
            for (mBitmap = 0; mBitmap < width; ++mBitmap) {
                j = pixels[width * m + mBitmap];
                grey = (j & 16711680) >> 16;
                redSum += (double) grey;
            }
        }

        m = (int) (redSum / total);

        for (mBitmap = 0; mBitmap < height; ++mBitmap) {
            for (j = 0; j < width; ++j) {
                grey = pixels[width * mBitmap + j];
                int alpha1 = -16777216;
                int red = (grey & 16711680) >> 16;
                int green = (grey & '\uff00') >> 8;
                int blue = grey & 255;
                short var17;
                short var18;
                short var19;
                if (red >= m) {
                    var19 = 255;
                    var18 = 255;
                    var17 = 255;
                } else {
                    var19 = 0;
                    var18 = 0;
                    var17 = 0;
                }

                grey = alpha1 | var17 << 16 | var18 << 8 | var19;
                pixels[width * mBitmap + j] = grey;
            }
        }

        Bitmap var16 = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        var16.setPixels(pixels, 0, width, 0, 0, width, height);
        return var16;
    }

    private byte[] getbmpdataTsc(int[] b, int w, int h) {
        int n = (w + 7) / 8;
        byte[] data = new byte[n * h];
        byte mask = 1;

        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < n * 8; ++x) {
                if (x < w) {
                    if ((b[y * w + x] & 16711680) >> 16 != 0) {
                        data[y * n + x / 8] |= (byte) (mask << 7 - x % 8);
                    }
                } else if (x >= w) {
                    data[y * n + x / 8] |= (byte) (mask << 7 - x % 8);
                }
            }
        }

        return data;
    }

    public Bitmap getResizedBitmap(Bitmap image) {
        int width = 380;
        int height = 280;
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}
