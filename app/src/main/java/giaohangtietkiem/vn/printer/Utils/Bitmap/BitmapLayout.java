package giaohangtietkiem.vn.printer.Utils.Bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;

public class BitmapLayout {

    public static int COMPONENT_TEXT_CONTENT = 1;
    public static int COMPONENT_BOLD_TEXT_CONTENT = 2;
    public static int COMPONENT_IMAGE_CONTENT = 3;
    public static int COMPONENT_BARCODE_CONTENT = 4;
    public static int COMPONENT_LINE_CONTENT = 5;

    private boolean autoVertical = false;
    private int currentY = 0;
    //    private int oldTextPositionForLine = 0;
    private int width = 0;
    private int height = 0;
    private Bitmap bitmap = null;
    private int padding = 0;
    private int paddingLeft = 0;
    private int paddingRight = 0;
    private int paddingTop = 0;
    private int paddingBottom = 0;
    private float boderRadius = 0;
    private Context context;
    private ArrayList<BitmapLayoutComponent> componentList = new ArrayList<>();
    private ArrayList<Integer> componentIndex = new ArrayList<>();

    public BitmapLayout(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;

        this.bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    public void setAutoVertical(boolean state) {
        this.autoVertical = state;
    }

    public Bitmap createBitmapLayout() {
        if (autoVertical) {
            currentY = componentList.get(0).y;
        }

        if (bitmap != null) {
            try {
                Canvas canvas = new Canvas(this.bitmap);
                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                addBlackBorder(bitmap, paint, canvas);
                paint.setStyle(Paint.Style.FILL);

                paint.setColor(Color.BLACK);

                //draw components
                for (int i = 0; i < componentIndex.size(); i++) {
                    int index = componentIndex.get(i);
                    BitmapLayoutComponent component = componentList.get(i);
                    switch (index) {
                        case 1: {
                            if (component.maxLine > 0) {
                                drawNormalText(paint, canvas, component.text, component.textSize, component.x, component.y, component.maxLine);
                            } else {
                                drawNormalText(paint, canvas, component.text, component.textSize, component.x, component.y);
                            }
                            break;
                        }
                        case 2: {
                            if (component.paddingBottom != 0 || component.paddingTop != 0) {
                                drawBoldTextPadding(paint, canvas, component.text, component.textSize, component.x, component.y, component.centerHorizontal, component.paddingTop, component.paddingBottom);
                            } else {
                                drawBoldText(paint, canvas, component.text, component.textSize, component.x, component.y, component.centerHorizontal);
                            }
                            break;
                        }
                        case 3: {
                            Bitmap largeImage = BitmapFactory.decodeResource(context.getResources(), component.imageSource);
                            Bitmap bitmapScaled = Bitmap.createScaledBitmap(largeImage, component.width, component.height, false);
                            if (autoVertical) {
                                component.y = currentY;
                            }
                            int xPosition = component.x;
                            int yPosition = component.y;
                            if (component.centerHorizontal) {
                                xPosition = (canvas.getWidth() / 2) - bitmapScaled.getWidth() / 2;
                            }
                            if (component.centerVertical) {
                                yPosition = (canvas.getHeight() / 2) - bitmapScaled.getHeight() / 2;
                            }
                            canvas.drawBitmap(bitmapScaled, xPosition, yPosition, paint);
                            if (autoVertical) {
                                currentY += bitmapScaled.getHeight() + 5;// + (bitmapScaled.getHeight() / 1.5);
                            }
                            break;
                        }
                        case 4: {
                            if (autoVertical) {
                                component.y = currentY;
                            }
                            if (component.centerHorizontal) {
                                component.x = (canvas.getWidth() / 2) - (component.width / 2);
                            }
                            drawBarCode(paint, canvas, component.text, component.x, component.y, component.width, component.height);
                            if (autoVertical) {
                                currentY += component.height / 2;
                            }
                            break;
                        }
                        case 5: {
                            paint.setStrokeWidth(1);
                            if (autoVertical) {
                                component.y = currentY;
//                                component.y = oldTextPositionForLine;
                            }
                            component.y = component.y + component.marginTop;

                            canvas.drawLine(component.x, component.y, (component.x + component.width), (component.y + component.height), paint);
                            if (autoVertical) {
                                currentY += component.height + component.marginBottom;
                            }
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    public void setBorderRadius(float radius) {
        this.boderRadius = radius;
    }

    public void setPadding(int top, int left, int right, int bottom) {
        this.paddingTop = top;
        this.paddingBottom = bottom;
        this.paddingLeft = left;
        this.paddingRight = right;
    }

    private void addBlackBorder(Bitmap bitmap, Paint paint, Canvas canvas) {
        final int borderSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) this.padding, this.context.getResources().getDisplayMetrics());
        final int cornerSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.boderRadius, this.context.getResources().getDisplayMetrics());

        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);

        // prepare canvas for transfer
        paint.setAntiAlias(true);
        paint.setColor(0xFFFFFFFF);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        // draw border
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth((float) borderSizePx);
        paint.setStrokeWidth(0f);
        canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);
    }

    public void setPadding(int size) {
        this.padding = size;
    }

    private void drawNormalText(Paint paint, Canvas canvas, String text, float textSize, int x, int y) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(textSize);

        float mesuare = paint.measureText(text);
        int canvasWidth = canvas.getWidth();
        float tempx = mesuare / canvasWidth;
        int tMax = 1;
        for (int i = 0; i < tempx; i++) {
            tMax += 1;
        }

        int stringUnit = text.length() / tMax;
        stringUnit += 13;
        int yMove = y;
        if (autoVertical) {
            yMove = currentY;
        }

        String strSub = "";
        while (text.length() != 0) {
            if (stringUnit <= text.length()) {
//                strSub = text.substring(0, stringUnit);
                int newIndex = stringUnit;
                while (paint.measureText(text.substring(0, newIndex)) < (width - 25) && newIndex < text.length()) {
                    newIndex += 1;
                }
                strSub = text.substring(0, newIndex);
                text = text.substring(strSub.length(), text.length());
            } else {
                strSub = text.substring(0, text.length());
                text = text.substring(strSub.length(), text.length());
            }
            canvas.drawText(strSub, x, yMove, paint);
            yMove += textSize;
        }
        if (autoVertical) {
            currentY = yMove;
        }
    }

    private void drawNormalText(Paint paint, Canvas canvas, String text, float textSize, int x, int y, int maxLine) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        paint.setTextSize(textSize);

        float mesuare = paint.measureText(text);
        int canvasWidth = canvas.getWidth();
        float tempx = mesuare / canvasWidth;
        int tMax = 1;
        for (int i = 0; i < tempx; i++) {
            tMax += 1;
        }

        int stringUnit = text.length() / tMax;
        stringUnit += 13;
        int yMove = y;
        if (autoVertical) {
            yMove = currentY;
        }

        String strSub = "";
        int line = 1;
        while (text.length() != 0) {
            if (stringUnit <= text.length()) {
//                strSub = text.substring(0, stringUnit);
                int newIndex = stringUnit;
                while (paint.measureText(text.substring(0, newIndex)) < (width - 25) && newIndex < text.length()) {
                    newIndex += 1;
                }
                strSub = text.substring(0, newIndex);
                text = text.substring(strSub.length(), text.length());
            } else {
                strSub = text.substring(0, text.length());
                text = text.substring(strSub.length(), text.length());
            }
            if (line == maxLine && maxLine > 1) {
                strSub += "...";
            }

            canvas.drawText(strSub, x, yMove, paint);
            yMove += textSize;

            if (line == maxLine) {
                break;
            }
            line += 1;
        }
        if (autoVertical) {
            currentY = yMove;
        }
    }

    private void drawBoldTextPadding(Paint paint, Canvas canvas, String text, float textSize, int x, int y, boolean centerHorizontal, int paddingTop, int paddingBottom) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        if (autoVertical) {
//            currentY += (textSize * 0.5);
            y = currentY;
        }
        y += paddingTop;
        if (centerHorizontal) {
            x = (canvas.getWidth() / 2) - (int) (paint.measureText(text) / 2);
        }
        canvas.drawText(text, x, y, paint);
        if (autoVertical) {
//            oldTextPositionForLine = currentY + (paddingBottom + 5) + 5;
            currentY = currentY + (int) (textSize * 0.5);
            currentY += paddingBottom;
        }
    }

    private void drawBoldText(Paint paint, Canvas canvas, String text, float textSize, int x, int y, boolean centerHorizontal) {
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        paint.setTextSize(textSize);
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        if (autoVertical) {
//            currentY += (currentY) * 0.5;
            y = currentY;
        }
        if (centerHorizontal) {
            x = (canvas.getWidth() / 2) - (int) (paint.measureText(text) / 2);
        }
        canvas.drawText(text, x, y, paint);
        if (autoVertical) {
//            oldTextPositionForLine = currentY + 5;
            currentY = currentY + (int) (textSize * 0.5);
        }
    }

    private void drawBarCode(Paint paint, Canvas canvas, String barCode, int x, int y, int width, int height) {
        try {
            Bitmap bitmapBarCode = this.encodeAsBitmap(barCode, BarcodeFormat.CODE_128, width, height);
            if (autoVertical) {
                y = currentY;
            }
            canvas.drawBitmap(bitmapBarCode, x, y, paint);
            if (autoVertical) {
                currentY = currentY + height;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

    private Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = contents;
        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public void addComponent(Integer type, BitmapLayoutComponent component) {
        if (component != null) {
            componentIndex.add(type);
            componentList.add(component);
        }
    }
}
