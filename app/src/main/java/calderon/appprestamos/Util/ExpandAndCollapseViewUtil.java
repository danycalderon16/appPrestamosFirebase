package calderon.appprestamos.Util;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import java.lang.reflect.Method;

public class ExpandAndCollapseViewUtil {
    public static void expand(ViewGroup v, int duration) {
        slide(v, duration, true);
    }

    public static void collapse(ViewGroup v, int duration) {
        slide(v, duration, false);
    }

    @SuppressLint("WrongConstant")
    private static void slide(final ViewGroup v, int duration, final boolean expand) {
        try {
            Method m = v.getClass().getDeclaredMethod("onMeasure", new Class[]{Integer.TYPE, Integer.TYPE});
            m.setAccessible(true);
            m.invoke(v, new Object[]{Integer.valueOf(MeasureSpec.makeMeasureSpec(((View) v.getParent()).getMeasuredWidth(), Integer.MIN_VALUE)), Integer.valueOf(MeasureSpec.makeMeasureSpec(0, 0))});
        } catch (Exception e) {
            Log.e("slideAnimation", e.getMessage(), e);
        }
        final int initialHeight = v.getMeasuredHeight();
        if (expand) {
            v.getLayoutParams().height = 0;
        } else {
            v.getLayoutParams().height = initialHeight;
        }
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            /* access modifiers changed from: protected */
            public void applyTransformation(float interpolatedTime, Transformation t) {
                int newHeight;
                if (expand) {
                    newHeight = (int) (((float) initialHeight) * interpolatedTime);
                } else {
                    newHeight = (int) (((float) initialHeight) * (1.0f - interpolatedTime));
                }
                v.getLayoutParams().height = newHeight;
                v.requestLayout();
                if (interpolatedTime == 1.0f && !expand) {
                    v.setVisibility(View.GONE);
                }
            }

            public boolean willChangeBounds() {
                return true;
            }
        };
        a.setDuration((long) duration);
        v.startAnimation(a);
    }
}
