package com.otaliastudios.cameraview.internal;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.otaliastudios.cameraview.controls.Grid;

/**
 * A layout overlay that draws grid lines based on the {@link Grid} parameter.
 */
public class GridLinesLayout extends View {

    private final static float GOLDEN_RATIO_INV = 0.61803398874989f;
    public final static int DEFAULT_COLOR = Color.argb(160, 255, 255, 255);

    private Grid gridMode;
    private int gridColor = DEFAULT_COLOR;

    private ColorDrawable horiz;
    private ColorDrawable vert;

    private Paint linePaint;

    private final float width;

    interface DrawCallback {
        void onDraw(int lines);
    }

    @VisibleForTesting DrawCallback callback;

    public GridLinesLayout(@NonNull Context context) {
        this(context, null);
    }

    public GridLinesLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        horiz = new ColorDrawable(gridColor);
        vert = new ColorDrawable(gridColor);
        width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.9f,
                context.getResources().getDisplayMetrics());
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        horiz.setBounds(left, 0, right, (int) width);
        vert.setBounds(0, top, (int) width, bottom);
    }

    /**
     * Returns the current grid value.
     * @return the grid mode
     */
    @NonNull
    public Grid getGridMode() {
        return gridMode;
    }

    /**
     * Sets a new grid value
     * @param gridMode the new value
     */
    public void setGridMode(@NonNull Grid gridMode) {
        this.gridMode = gridMode;
        postInvalidate();
    }

    /**
     * Returns the current grid color.
     * @return the grid color
     */
    public int getGridColor() {
        return gridColor;
    }

    /**
     * Sets a new grid color.
     * @param gridColor the new color
     */
    public void setGridColor(@ColorInt int gridColor) {
        this.gridColor = gridColor;
        horiz.setColor(gridColor);
        vert.setColor(gridColor);
        postInvalidate();
    }

    private int getLineCount() {
        switch (gridMode) {
            case OFF: return 0;
            case DRAW_3X3: return 2;
            case DRAW_PHI: return 2;
            case DRAW_4X4: return 3;
            case DRAW_CROSS: return 1;     // 1 đường ngang + 1 đường dọc
            case DRAW_DIAGONAL: return 1;  // 2 đường chéo
            case DRAW_FIBONACCI: return -1; // special case
        }
        return 0;
    }

    private float getLinePosition(int lineNumber) {
        int lineCount = getLineCount();
        if (gridMode == Grid.DRAW_PHI) {
            // 1 = 2x + GRIx
            // 1 = x(2+GRI)
            // x = 1/(2+GRI)
            float delta = 1f/(2+GOLDEN_RATIO_INV);
            return lineNumber == 1 ? delta : (1 - delta);
        } else {
            return (1f / (lineCount + 1)) * (lineNumber + 1f);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

        if (gridMode == Grid.DRAW_CROSS) {
            // Vẽ dấu cộng: đường giữa ngang và dọc
            float midX = getWidth() / 2f;
            float midY = getHeight() / 2f;

            // Horizontal
            canvas.drawLine(0, midY, getWidth(), midY, getPaint());
            // Vertical
            canvas.drawLine(midX, 0, midX, getHeight(), getPaint());

        } else if (gridMode == Grid.DRAW_DIAGONAL) {
            // Vẽ dấu nhân (×)
            canvas.drawLine(0, 0, getWidth(), getHeight(), getPaint());
            canvas.drawLine(getWidth(), 0, 0, getHeight(), getPaint());

        } else if (gridMode == Grid.DRAW_FIBONACCI) {
            // Vẽ Fibonacci spiral bằng cung tròn (quarter arcs)
            drawFibonacci(canvas);

        } else {
            // Giữ logic cũ cho 3x3, 4x4, phi
            int count = getLineCount();
            for (int n = 0; n < count; n++) {
                float pos = getLinePosition(n);

                // Horizontal
                canvas.translate(0, pos * getHeight());
                horiz.draw(canvas);
                canvas.translate(0, - pos * getHeight());

                // Vertical
                canvas.translate(pos * getWidth(), 0);
                vert.draw(canvas);
                canvas.translate(- pos * getWidth(), 0);
            }
            if (callback != null) {
                callback.onDraw(count);
            }
        }
    }

    private void drawFibonacci(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        int min = Math.min(w, h);

        // Một số Fibonacci để vẽ (có thể mở rộng thêm)
        int[] fib = {1, 1, 2, 3, 5, 8, 13};

        // Tính scale sao cho vừa màn hình
        float scale = (float) min / fib[fib.length - 1];

        // Điểm bắt đầu
        float x = (w - min) / 2f;
        float y = (h - min) / 2f;

        int dir = 0; // 0: phải, 1: xuống, 2: trái, 3: lên
        for (int i = 0; i < fib.length; i++) {
            float size = fib[i] * scale;
            RectF rect;

            switch (dir) {
                case 0: // phải
                    rect = new RectF(x, y, x + size, y + size);
                    canvas.drawArc(rect, 90, 90, false, getPaint());
                    x += size;
                    break;
                case 1: // xuống
                    rect = new RectF(x - size, y, x, y + size);
                    canvas.drawArc(rect, 0, 90, false, getPaint());
                    y += size;
                    break;
                case 2: // trái
                    rect = new RectF(x - size, y - size, x, y);
                    canvas.drawArc(rect, 270, 90, false, getPaint());
                    x -= size;
                    break;
                case 3: // lên
                    rect = new RectF(x, y - size, x + size, y);
                    canvas.drawArc(rect, 180, 90, false, getPaint());
                    y -= size;
                    break;
            }
            dir = (dir + 1) % 4;
        }
    }



    private Paint getPaint() {
        if (linePaint == null) {
            linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            linePaint.setStyle(Paint.Style.STROKE);
            linePaint.setStrokeWidth(width);
            linePaint.setColor(gridColor);
        }
        return linePaint;
    }
}
