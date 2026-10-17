package com.example.zzzznotch.data.models;

public class NotchAppearance {
    public enum ShapeType {
        ROUNDED_RECTANGLE("Rounded Rectangle"),
        PILL("Pill / Stadium"),
        OVAL("Oval / Circle"),
        CUSTOM_CUTOUT("Cutout Standard");

        private final String displayName;

        ShapeType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum PhotoOverlayMode {
        ALWAYS_VISIBLE("Always Visible"),
        VISIBLE_ON_TOUCH("Visible Only On Touch"),
        DISABLED("Disabled");

        private final String displayName;

        PhotoOverlayMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private int widthDp;
    private int heightDp;
    private int offsetXDp;
    private int offsetYDp;
    private int backgroundColor;
    private int cornerRadiusDp;
    private float alpha;
    private ShapeType shapeType;
    private String photoUri;
    private PhotoOverlayMode photoOverlayMode;
    private int imagePaddingDp;
    private boolean alignCenter;

    public NotchAppearance() {
        this.widthDp = 120;
        this.heightDp = 36;
        this.offsetXDp = 0;
        this.offsetYDp = 0;
        this.backgroundColor = 0xFF000000; // Solid black
        this.cornerRadiusDp = 18;
        this.alpha = 1.0f;
        this.shapeType = ShapeType.PILL;
        this.photoUri = "";
        this.photoOverlayMode = PhotoOverlayMode.DISABLED;
        this.imagePaddingDp = 2;
        this.alignCenter = true;
    }

    public int getWidthDp() {
        return widthDp;
    }

    public void setWidthDp(int widthDp) {
        this.widthDp = widthDp;
    }

    public int getHeightDp() {
        return heightDp;
    }

    public void setHeightDp(int heightDp) {
        this.heightDp = heightDp;
    }

    public int getOffsetXDp() {
        return offsetXDp;
    }

    public void setOffsetXDp(int offsetXDp) {
        this.offsetXDp = offsetXDp;
    }

    public int getOffsetYDp() {
        return offsetYDp;
    }

    public void setOffsetYDp(int offsetYDp) {
        this.offsetYDp = offsetYDp;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getCornerRadiusDp() {
        return cornerRadiusDp;
    }

    public void setCornerRadiusDp(int cornerRadiusDp) {
        this.cornerRadiusDp = cornerRadiusDp;
    }

    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri != null ? photoUri : "";
    }

    public PhotoOverlayMode getPhotoOverlayMode() {
        return photoOverlayMode;
    }

    public void setPhotoOverlayMode(PhotoOverlayMode photoOverlayMode) {
        this.photoOverlayMode = photoOverlayMode;
    }

    public int getImagePaddingDp() {
        return imagePaddingDp;
    }

    public void setImagePaddingDp(int imagePaddingDp) {
        this.imagePaddingDp = imagePaddingDp;
    }

    public boolean isAlignCenter() {
        return alignCenter;
    }

    public void setAlignCenter(boolean alignCenter) {
        this.alignCenter = alignCenter;
    }
}
