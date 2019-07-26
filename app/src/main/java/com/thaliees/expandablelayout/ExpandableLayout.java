package com.thaliees.expandablelayout;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public class ExpandableLayout extends FrameLayout {
    // Define the states
    private static final int COLLAPSED = 0;
    private static final int COLLAPSING = 1;
    private static final int EXPANDING = 2;
    private static final int EXPANDED = 3;
    // Define the key
    public static final String KEY_SUPER_STATE = "super_state";
    public static final String KEY_EXPANSION = "expansion";
    // Define the orientation for our Expandable
    private static final int HORIZONTAL = 0;
    private static final int VERTICAL = 1;
    // Define a default duration (for expanded or collapsed)
    private static final int DEFAULT_DURATION = 300;
    // Define our variables to use
    private int duration = DEFAULT_DURATION, orientation, state;
    private float parallax, expansion;

    // Define the type of Interpolator you want to use
    private Interpolator interpolator = new FastOutSlowInInterpolator();
    private ValueAnimator animator;
    // Only if you use interface
    private OnExpansionUpdateListener listener;

    public ExpandableLayout(Context context) {
        super(context, null);
    }

    public ExpandableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        // If the attributes were defined from the design
        if(attrs != null){
            TypedArray properties = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableLayout);
            // Time in seconds
            duration = properties.getInt(R.styleable.ExpandableLayout_duration, DEFAULT_DURATION);
            // If it shows expanded or not, our ExpandableLayout, by default is false
            expansion = properties.getBoolean(R.styleable.ExpandableLayout_expanded, false) ? 1 : 0;
            // The orientation in which it will do the expanded or collapsed, by default is VERTICAL
            orientation = properties.getInt(R.styleable.ExpandableLayout_android_orientation, VERTICAL);
            // Distance by which hidden items will be shown
            parallax = properties.getFloat(R.styleable.ExpandableLayout_parallax, 1);
            properties.recycle();

            // Define the state of our ExpandableLayout
            state = expansion == 1 ? EXPANDED : COLLAPSED;
            setParallax(parallax);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        // Get the current stateLayout value
        expansion = isExpandedLayout() ? 1 : 0;
        Parcelable superState = super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putFloat(KEY_EXPANSION, expansion);
        bundle.putParcelable(KEY_SUPER_STATE, superState);

        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable parcelable) {
        Bundle bundle = (Bundle) parcelable;
        expansion = bundle.getFloat(KEY_EXPANSION);
        state = expansion == 1 ? EXPANDED : COLLAPSED;
        Parcelable superState = bundle.getParcelable(KEY_SUPER_STATE);
        super.onRestoreInstanceState(superState);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // Get width of view that not drawn yet.
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();

        // Define the final view size
        int size = orientation == LinearLayout.HORIZONTAL ? width : height;

        // Define whether the view will be displayed or not
        setVisibility(expansion == 0 && size == 0 ? GONE : VISIBLE);

        int expansionDelta = size - Math.round(size * expansion);
        if (parallax > 0) {
            float parallaxDelta = expansionDelta * parallax;
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (orientation == HORIZONTAL) {
                    int direction = -1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
                        direction = 1;
                    }
                    child.setTranslationX(direction * parallaxDelta);
                }
                else child.setTranslationY(-parallaxDelta);
            }
        }

        if (orientation == HORIZONTAL) setMeasuredDimension(width - expansionDelta, height);
        else setMeasuredDimension(width, height - expansionDelta);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        if (animator != null) {
            animator.cancel();
        }
        super.onConfigurationChanged(newConfig);
    }

    public int getDuration() { return duration; }

    public void setDuration(int duration) { this.duration = duration; }

    public int getOrientation() { return orientation; }

    public void setOrientation(int orientation) {
        if (orientation < 0 || orientation > 1) {
            throw new IllegalArgumentException("Orientation must be either 0 (horizontal) or 1 (vertical)");
        }
        this.orientation = orientation;
    }

    public float getParallax() {
        return parallax;
    }

    public void setParallax(float parallax) {
        // Make sure parallax is between 0 and 1
        parallax = Math.min(1, Math.max(0, parallax));
        this.parallax = parallax;
    }

    public void setInterpolator(Interpolator interpolator) { this.interpolator = interpolator; }

    public void setOnExpansionUpdateListener(OnExpansionUpdateListener listener) { this.listener = listener; }

    public boolean isExpandedLayout() {
        return state == EXPANDING || state == EXPANDED;
    }

    // Animation Toggle with default animation
    public void toggle() {
        toggle(true);
    }

    // Animation Toggle, defining if we want animation or not
    public void toggle(boolean animate) {
        if (isExpandedLayout()) collapse(animate);
        else expand(animate);
    }

    // Animation Expand with default animation
    public void expand() { expand(true); }

    // Animation Expand, defining if we want animation or not
    public void expand(boolean animate) { setMovement(true, animate); }

    // Animation Collapse with default animation
    public void collapse() { collapse(true); }

    // Animation Collapse, defining if we want animation or not
    public void collapse(boolean animate) { setMovement(false, animate); }

    // Evaluate the type of movement
    public void setMovement(boolean isExpanded, boolean animate) {
        if (isExpanded == isExpandedLayout()) return;

        int targetExpansion = isExpanded ? 1 : 0;

        if (animate) animateSize(targetExpansion);
        else setExpansion(targetExpansion);
    }

    // Movement without animation
    public void setExpansion(float expansion) {
        if (this.expansion == expansion) {
            return;
        }

        // Infer stateLayout from previous value
        float delta = expansion - this.expansion;
        if (expansion == 0) {
            state = COLLAPSED;
        } else if (expansion == 1) {
            state = EXPANDED;
        } else if (delta < 0) {
            state = COLLAPSING;
        } else if (delta > 0) {
            state = EXPANDING;
        }

        setVisibility(state == COLLAPSED ? GONE : VISIBLE);
        this.expansion = expansion;
        requestLayout();

        // If you use the interface from your activity
        if (listener != null) {
            listener.onExpansionUpdate(expansion, state);
            listener.onExpansionUpdate(expansion);
        }
    }

    // Movement with animation
    private void animateSize(int targetExpansion) {
        if (animator != null) {
            animator.cancel();
            animator = null;
        }

        // Indicate the current point of our ExpandableLayout is and the point where it should arrive
        animator = ValueAnimator.ofFloat(expansion, targetExpansion);
        // Set the Interpolator
        animator.setInterpolator(interpolator);
        // Set the duration
        animator.setDuration(duration);
        // Update the width or height of our ExpandableLayout
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                setExpansion((float) valueAnimator.getAnimatedValue());
            }
        });
        // Add our Animator
        animator.addListener(new ExpansionListener(targetExpansion));
        // Animation start
        animator.start();
    }

    private class ExpansionListener implements Animator.AnimatorListener {
        private int targetExpansion;
        private boolean isCanceled;

        private ExpansionListener(int targetExpansion) { this.targetExpansion = targetExpansion; }

        @Override
        public void onAnimationStart(Animator animation) {
            // Define the current state to start
            // Will the layout collapsing or expanding?
            state = targetExpansion == 0 ? COLLAPSING : EXPANDING;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            if (!isCanceled) {
                // Define the current state to end
                // Will the layout collapsed or expanded?
                state = targetExpansion == 0 ? COLLAPSED : EXPANDED;
                setExpansion(targetExpansion);
            }
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            isCanceled = true;
        }

        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    }

    // If you want to play with the expansion values or the current states of the ExpandableLayout
    public interface OnExpansionUpdateListener {
        /**
         * Callback for expansion updates
         *
         * @param expansionFraction Value between 0 (collapsed) and 1 (expanded) representing the the expansion progress
         * @param state             Representing the current expansion stateLayout
         */
        void onExpansionUpdate(float expansionFraction, int state);
        // Or simply
        //void onExpansionUpdate(int state);
        void onExpansionUpdate(float expansionFraction);
    }
}
