package com.andersonblough.floatinglabeledittext;

import android.animation.Animator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * @author andersonblough (bill@akta.com)
 *
 * add a FloatingLavelEditText to your project by wrapping an editText view in your xml
 *
 * you can add customize the appearance of your label using the attribute hint_text_appearance
 * you can customize the padding of your label using the hintPadding(Left, Top, Right, Bottom) attributes
 *
 * you can add customize the appearance of your error using the attribute error_text_appearance
 * you can customize the padding of your label using the errorMessagePadding(Left, Top, Right, Bottom) attributes
 * you can customize the right drawable of your error using the errorDrawable attribute
 *
 * when error is shown, the editText is set to activated so you can handle changes to your editText
 */
public class FloatingLabelEditText extends FrameLayout {

    private static final String HINT_TAG = "hint";
    private static final String ERROR_TAG = "error";

    Context mContext;

    private TextView mHintView;
    private TextView mErrorView;
    private EditText mEditText;
    private Drawable errorDrawable;

    public FloatingLabelEditText(Context context) {
        this(context, null);
    }

    public FloatingLabelEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        mHintView = new TextView(mContext);
        mErrorView = new TextView(mContext);

        TypedArray ta = mContext.obtainStyledAttributes(attrs, R.styleable.FloatingEditText);

        int hintPadding = ta.getDimensionPixelSize(R.styleable.FloatingEditText_hintPadding, 0);
        int hintPaddingLeft = ta.getDimensionPixelSize(R.styleable.FloatingEditText_hintPaddingLeft, 0);
        int hintPaddingTop = ta.getDimensionPixelSize(R.styleable.FloatingEditText_hintPaddingTop, 0);
        int hintPaddingRight = ta.getDimensionPixelSize(R.styleable.FloatingEditText_hintPaddingRight, 0);
        int hintPaddingBottom = ta.getDimensionPixelSize(R.styleable.FloatingEditText_hintPaddingBottom, 0);

        int errorPadding = ta.getDimensionPixelSize(R.styleable.FloatingEditText_errorMessagePadding, 0);
        int errorPaddingLeft = ta.getDimensionPixelSize(R.styleable.FloatingEditText_errorMessagePaddingLeft, 0);
        int errorPaddingTop = ta.getDimensionPixelSize(R.styleable.FloatingEditText_errorMessagePaddingTop, 0);
        int errorPaddingRight = ta.getDimensionPixelSize(R.styleable.FloatingEditText_errorMessagePaddingRight, 0);
        int errorPaddingBottom = ta.getDimensionPixelSize(R.styleable.FloatingEditText_errorMessagePaddingBottom, 0);

        errorDrawable = ta.getDrawable(R.styleable.FloatingEditText_errorDrawable);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mErrorView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, errorDrawable, null);
        } else {
            mErrorView.setCompoundDrawables(null, null, errorDrawable, null);
        }


        if (hintPadding != 0) {
            setViewPadding(mHintView, hintPadding, hintPadding, hintPadding, hintPadding);
        } else {
            setViewPadding(mHintView, hintPaddingLeft, hintPaddingTop, hintPaddingRight, hintPaddingBottom);
        }

        if (errorPadding != 0) {
            setViewPadding(mErrorView, errorPadding, errorPadding, errorPadding, errorPadding);
        } else {
            setViewPadding(mErrorView, errorPaddingLeft, errorPaddingTop, errorPaddingRight, errorPaddingBottom);
        }

        mHintView.setTextAppearance(mContext, ta.getResourceId(R.styleable.FloatingEditText_hintTextAppearance, android.R.style.TextAppearance_Small));
        mErrorView.setTextAppearance(mContext, ta.getResourceId(R.styleable.FloatingEditText_errorTextAppearance, android.R.style.TextAppearance_Small));

        mHintView.setVisibility(INVISIBLE);
        mHintView.setAlpha(0);
        mErrorView.setVisibility(INVISIBLE);

        mHintView.setTag(HINT_TAG);
        mErrorView.setTag(ERROR_TAG);

        addView(mHintView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        ta.recycle();
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {

        String tag = (String) child.getTag();
        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(params);

        if (tag != null && tag.equals(ERROR_TAG)) {
            lp.topMargin = mHintView.getHeight() + mEditText.getHeight();
        }

        if (child instanceof EditText) {
            if (mEditText != null) {
                throw new IllegalArgumentException("Can only have one EditText sub view");
            }

            lp.topMargin = (int) (mHintView.getTextSize() + mHintView.getTotalPaddingTop() + mHintView.getTotalPaddingBottom());

            setEditText((EditText) child);
        }
        params = lp;

        super.addView(child, index, params);
    }

    private void setEditText(EditText editText) {
        mEditText = editText;

        mEditText.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                setShowHint(!TextUtils.isEmpty(s));
            }
        });

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus && mHintView.getVisibility() == VISIBLE) {
                    mHintView.animate().alpha(1f).setListener(null).start();
                } else if (mHintView.getVisibility() == VISIBLE) {
                    mHintView.animate().alpha(0.33f).setListener(null).start();
                }

                if (hasFocus) {
                    hideError();
                }
            }
        });

        mHintView.setText(mEditText.getHint());
        if (!TextUtils.isEmpty(mEditText.getText())) {
            mHintView.setVisibility(VISIBLE);
        }
    }

    private void setShowHint(final boolean show) {
        float translationY = 0;
        float alpha = 0;

        if (mHintView.getVisibility() == VISIBLE && !show) {
            alpha = 0;
            translationY = mHintView.getHeight() / 8;
            animateHint(alpha, translationY, show);
        } else if (mHintView.getVisibility() != VISIBLE && show) {
            if (mEditText.isFocused()) {
                alpha = 1f;
            } else {
                alpha = 0.33f;
            }
            translationY = 0;
            animateHint(alpha, translationY, show);
        }
    }

    private void animateHint(float alpha, float translationY, final boolean show) {
        mHintView.animate().alpha(alpha).translationY(translationY).setListener(new SimpleAnimationListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mHintView.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHintView.setVisibility(show ? VISIBLE : INVISIBLE);
                mHintView.setAlpha(show ? 1 : 0);
            }
        }).start();
    }

    /**
     *
     * @return the reference to your {@link android.widget.EditText} supplied in xml
     */
    public EditText getEditText() {
        return mEditText;
    }

    /**
     *
     * @return the String value of the {@link android.widget.EditText}
     */
    public String getText() {
        return mEditText.getText().toString();
    }

    /**
     * show an error message
     * @param errorMessage
     */
    public void showError(String errorMessage) {

        if (errorMessage != null) {
            mErrorView.setText(errorMessage);
            mErrorView.setVisibility(VISIBLE);
            mEditText.setActivated(true);
            mEditText.clearFocus();
        }

        if (!mErrorView.isShown()) {
            addView(mErrorView, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        }

    }

    /**
     * show error message with right drawable
     * @param errorMessage
     * @param errorDrawable
     */
    public void showError(String errorMessage, Drawable errorDrawable){
        this.errorDrawable =  errorDrawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mErrorView.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, errorDrawable, null);
        } else {
            mErrorView.setCompoundDrawables(null, null, errorDrawable, null);
        }
        showError(errorMessage);
    }

    /**
     * hide error
     */
    public void hideError() {
        removeView(mErrorView);
        mErrorView.setVisibility(INVISIBLE);
        mEditText.setActivated(false);
    }

    private void setViewPadding(TextView view, int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setPaddingRelative(left, top, right, bottom);
        } else {
            view.setPadding(left, top, right, bottom);
        }
    }
}
