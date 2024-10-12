package net.polyv.android.player.common.utils.ui;

import static net.polyv.android.player.sdk.foundation.app.PLVApplicationContext.getString;
import static net.polyv.android.player.sdk.foundation.graphics.DisplaysKt.dp;
import static net.polyv.android.player.sdk.foundation.lang.PreconditionsKt.requireNotNull;
import static net.polyv.android.player.sdk.foundation.lang.ThreadsKt.postToMainThread;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.polyv.android.player.sdk.foundation.app.PLVApplicationContext;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

/**
 * @author suhongtao
 */
public class PLVToast {

    private static Toast lastShowToast = null;

    private ToastParam param;

    private Toast toast;

    private PLVToast(ToastParam param) {
        this.param = param;
        postToMainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                initToast();
                return null;
            }
        });
    }

    private void initToast() {
        toast = new Toast(param.context);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(param.showDuration);

        final TextView textView = new AppCompatTextView(param.context);
        textView.setMinWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setMaxWidth(dp(228).px());
        textView.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setMaxLines(param.maxLines > 0 ? param.maxLines : 4);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setText(param.text);
        textView.setTextColor(param.textColor);
        int horizontalPadding = dp(16).px();
        int verticalPadding = dp(10).px();

        if (param.drawableResId != 0) {
            textView.setMaxWidth(dp(228).px());
            textView.setPadding(dp(8).px(), 0, 0, 0);

            ImageView imageView = new AppCompatImageView(param.context);
            imageView.setImageResource(param.drawableResId);

            final LinearLayout linearLayout = new LinearLayout(param.context);
            linearLayout.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout.setGravity(Gravity.CENTER);
            linearLayout.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
            linearLayout.addView(imageView);
            linearLayout.addView(textView);
            linearLayout.post(new Runnable() {
                @Override
                public void run() {
                    GradientDrawable gradientDrawable = new GradientDrawable();
                    gradientDrawable.setColor(param.backgroundColor);
                    if (textView.getLayout() == null || textView.getLayout().getLineCount() > 1) {
                        gradientDrawable.setCornerRadius(dp(8).px());
                    } else {
                        gradientDrawable.setCornerRadius(dp(20).px());
                    }
                    linearLayout.setBackground(gradientDrawable);
                }
            });

            toast.setView(linearLayout);
            return;
        }

        textView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding);
        textView.post(new Runnable() {
            @Override
            public void run() {
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setColor(param.backgroundColor);
                if (textView.getLayout() == null || textView.getLayout().getLineCount() > 1) {
                    gradientDrawable.setCornerRadius(dp(8).px());
                } else {
                    gradientDrawable.setCornerRadius(dp(20).px());
                }
                textView.setBackground(gradientDrawable);
            }
        });

        toast.setView(textView);
    }

    public void show() {
        postToMainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (lastShowToast != null) {
                    lastShowToast.cancel();
                }
                if (toast != null) {
                    toast.show();
                }
                lastShowToast = toast;
                return null;
            }
        });
    }

    public void cancel() {
        postToMainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                if (toast != null) {
                    toast.cancel();
                    toast = null;
                }
                return null;
            }
        });
    }

    private static class ToastParam {
        private Context context;
        private CharSequence text;
        @ColorInt
        private int textColor = Color.parseColor("#F0F1F5");
        @ColorInt
        private int backgroundColor = Color.parseColor("#991B202D");
        @DrawableRes
        private int drawableResId;
        private int showDuration = Toast.LENGTH_SHORT;
        private int maxLines = 4;
    }

    public static class Builder {

        private final ToastParam param;

        private Builder() {
            param = new ToastParam();
        }

        public static Builder create() {
            return context(requireNotNull(PLVApplicationContext.getApplicationContext()));
        }

        public static Builder context(@NonNull Context context) {
            Builder toastBuilder = new Builder();
            toastBuilder.param.context = context.getApplicationContext();
            return toastBuilder;
        }

        public Builder setText(CharSequence text) {
            if (text == null) {
                param.text = "";
            } else {
                param.text = text;
            }
            return this;
        }

        public Builder setText(@StringRes int stringResId) {
            param.text = getString(stringResId);
            return this;
        }

        public Builder setTextColor(@ColorInt int textColor) {
            param.textColor = textColor;
            return this;
        }

        public Builder setBackgroundColor(@ColorInt int backgroundColor) {
            param.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setDrawable(@DrawableRes int drawableResId) {
            param.drawableResId = drawableResId;
            return this;
        }

        public Builder shortDuration() {
            param.showDuration = Toast.LENGTH_SHORT;
            return this;
        }

        public Builder longDuration() {
            param.showDuration = Toast.LENGTH_LONG;
            return this;
        }

        /**
         * @deprecated use {@link #shortDuration()} or {@link #longDuration()} instead
         */
        @Deprecated
        public Builder duration(int toastDuration) {
            if (toastDuration != Toast.LENGTH_SHORT
                    && toastDuration != Toast.LENGTH_LONG) {
                toastDuration = Toast.LENGTH_SHORT;
            }
            param.showDuration = toastDuration;
            return this;
        }

        public Builder setMaxLines(int maxLines) {
            param.maxLines = maxLines;
            return this;
        }

        public PLVToast build() {
            return new PLVToast(param);
        }

        public void show() {
            build().show();
        }
    }
}
