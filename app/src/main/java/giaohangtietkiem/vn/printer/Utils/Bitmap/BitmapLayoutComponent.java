package giaohangtietkiem.vn.printer.Utils.Bitmap;

public class BitmapLayoutComponent {

    public int imageSource = 0;
    public String text = "";
    public float textSize = 0;
    public int x = 0;
    public int y = 0;
    public int width = 0;
    public int height = 0;
    public boolean centerHorizontal = false;
    public boolean centerVertical = false;
    public int paddingTop = 0;
    public int paddingBottom = 0;
    public int marginTop = 0;
    public int marginBottom = 0;
    public int maxLine = 0;

    //construction with Image
    public BitmapLayoutComponent(int imageSource, int x, int y, int width, int height, boolean centerHorizontal, boolean centerVertical) {
        this.imageSource = imageSource;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.centerHorizontal = centerHorizontal;
        this.centerVertical = centerVertical;
    }

    //Construction with barcode
    public BitmapLayoutComponent(String text, int x, int y, int width, int height, boolean centerVertical, boolean centerHorizontal) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.centerVertical = centerVertical;
        this.centerHorizontal = centerHorizontal;
    }

    //Construction with text
    public BitmapLayoutComponent(String text, float textSize, int x, int y, boolean centerVertical, boolean centerHorizontal) {
        this.text = text;
        this.textSize = textSize;
        this.x = x;
        this.y = y;
        this.centerHorizontal = centerHorizontal;
        this.centerVertical = centerVertical;
    }

    public BitmapLayoutComponent(String text, float textSize, int x, int y, boolean centerVertical, boolean centerHorizontal, int maxLine) {
        this.text = text;
        this.textSize = textSize;
        this.x = x;
        this.y = y;
        this.centerHorizontal = centerHorizontal;
        this.centerVertical = centerVertical;
        this.maxLine = maxLine;
    }

    //Construction with text with padding
    public BitmapLayoutComponent(String text, float textSize, int x, int y, boolean centerVertical, boolean centerHorizontal, int paddingTop, int paddingBottom) {
        this.text = text;
        this.textSize = textSize;
        this.x = x;
        this.y = y;
        this.centerHorizontal = centerHorizontal;
        this.centerVertical = centerVertical;
        this.paddingTop = paddingTop;
        this.paddingBottom = paddingBottom;
    }

    //Construction with line
    public BitmapLayoutComponent(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    //Construction with line
    public BitmapLayoutComponent(int x, int y, int width, int height, int marginTop, int marginBottom) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;
    }

    public BitmapLayoutComponent(int imageSource, String text, float textSize, int x, int y, int width, int height, boolean centerHorizontal, boolean centerVertical) {
        this.imageSource = imageSource;
        this.text = text;
        this.textSize = textSize;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.centerHorizontal = centerHorizontal;
        this.centerVertical = centerVertical;
    }
}
