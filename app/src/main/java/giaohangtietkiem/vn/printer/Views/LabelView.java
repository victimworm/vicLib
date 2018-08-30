package giaohangtietkiem.vn.printer.Views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.EnumMap;
import java.util.Map;

import giaohangtietkiem.vn.printer.Objects.Package;
import giaohangtietkiem.vn.printer.R;

public class LabelView {
    private Context context;

    private LabelView(Context mContext) {
        this.context = mContext;
    }

    private static LabelView INSTANCE = null;

    public static LabelView getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new LabelView(context);
        }
        return INSTANCE;
    }

    private Bitmap createBitmapFromView(View v) {
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
//        v.setLayoutParams(new LinearLayout.LayoutParams(384, 385));
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
//        Bitmap bitmap = Bitmap.createBitmap(384, 385, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(bitmap);
        v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);

        return Bitmap.createScaledBitmap(bitmap, 384, 385, false);
    }

    public Bitmap generateBitmap(Package pkg) {
        if (context == null) {
            return null;
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View label = inflater.inflate(R.layout.layout_label, null);
        ImageView barcode = label.findViewById(R.id.layout_label_barcode);
        TextView txtAlias = label.findViewById(R.id.layout_label_txt_alias);
        txtAlias.setTextSize(40);

        TextView txtRouterCode = label.findViewById(R.id.layout_label_txt_router_code);
        txtRouterCode.setTextSize(35);

        ImageView imgTruck = label.findViewById(R.id.layout_label_img_truck);

        String barCode = pkg.order;
        Bitmap bitmapBarCode = null;
        try {
            bitmapBarCode = this.encodeAsBitmap(barCode, BarcodeFormat.CODE_128, 360, 85);
        } catch (Exception e) {

        }
        if (bitmapBarCode != null) {
            barcode.setImageBitmap(bitmapBarCode);
        }

        if (pkg.alias.contains("Bộ") || pkg.alias.contains("bộ") || pkg.alias.contains("BO") || pkg.alias.contains("bo")) {
            String deliverCartCode = "BO." + pkg.deliver_cart_alias;
            imgTruck.setVisibility(View.VISIBLE);
            txtRouterCode.setText(deliverCartCode);
        } else {
            imgTruck.setVisibility(View.GONE);
            txtRouterCode.setText(pkg.deliver_cart_alias);
        }

        txtAlias.setText(pkg.order);
        TextView txtShopInfo = label.findViewById(R.id.layout_label_txt_shop_info);
        txtShopInfo.setTextSize(20);

        TextView txtCustomerInfo = label.findViewById(R.id.layout_label_txt_customer_info);
        txtCustomerInfo.setTextSize(20);

        TextView txtProductionInfo = label.findViewById(R.id.layout_label_txt_production_info);
        txtProductionInfo.setTextSize(20);

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

        if (pkg.shop_code.equals("S14268")) {
            created_username = "SHOPEE";
        }

        created_username += clientId;

        txtShopInfo.setText(created_username);
        String customerInfo = pkg.customer_fullname + ", " + pkg.customer_tel + ", " + pkg.customer_first_address + ", " + pkg.customer_last_address;
        txtCustomerInfo.setText(customerInfo);

        if (pkg.productList.size() > 0) {
            String productsName = pkg.productList.get(0);//.product_name;
            txtProductionInfo.setText(productsName);
        }

        Bitmap bitmap = createBitmapFromView(label);

        return bitmap;
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
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
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
}
