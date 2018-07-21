package giaohangtietkiem.vn.printer.Utils.Devices;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import net.posprinter.utils.BitmapToByteData;
import net.posprinter.utils.DataForSendToPrinterTSC;

import java.io.UnsupportedEncodingException;

public class SendDataToPrinterThread extends Thread {

    Bitmap mBitmap;
    public WifiCommunication wfComm = null;
    public DeviceConstant.PrinterStatus printerStatus;

    public SendDataToPrinterThread(Bitmap mBitmap, Context context, DeviceConstant.PrinterStatus printerStatus) {
        this.mBitmap = mBitmap;
        this.printerStatus = printerStatus;
    }

    @Override
    public void run() {
        super.run();

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
                }
            } finally {
                if (wfComm != null) {
                    wfComm.closeSocket();
                }
            }
        }
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

    private byte[] downLoadBmpToSendTSCData(Bitmap mBitmap, BitmapToByteData.BmpType bmpType) {
        Bitmap bitmap = toGrayscale(mBitmap);
        bitmap = convertGreyImg(bitmap);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] data = getbmpdataTsc(pixels, width, height);
        return data;
    }

    private Bitmap toGrayscale(Bitmap bmpOriginal) {
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

    private Bitmap convertGreyImg(Bitmap img) {
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
//                int green = (grey & '\uff00') >> 8;
//                int blue = grey & 255;
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
