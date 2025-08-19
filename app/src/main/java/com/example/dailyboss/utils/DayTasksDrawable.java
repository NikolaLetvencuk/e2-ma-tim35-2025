package com.example.dailyboss.utils;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.dailyboss.dto.TaskItemDto;

import java.util.List;

public class DayTasksDrawable extends Drawable {

    private List<TaskItemDto> tasks;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DayTasksDrawable(List<TaskItemDto> tasks) {
        this.tasks = tasks;
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int width = getBounds().width();
        int height = getBounds().height();

        float lineHeight = 4f;

        for (TaskItemDto task : tasks) {
            int color = Color.parseColor(task.getColor());
            paint.setColor(color);

            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(task.getStartTime());
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);

            float y = height * ((hour + minute / 60f) / 24f);

            float left = 0;
            float right = width;
            float top = y - lineHeight/2;
            float bottom = y + lineHeight/2;

            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    @Override public void setAlpha(int alpha) {}
    @Override public void setColorFilter(@Nullable ColorFilter colorFilter) {}
    @Override public int getOpacity() { return PixelFormat.OPAQUE; }
}

