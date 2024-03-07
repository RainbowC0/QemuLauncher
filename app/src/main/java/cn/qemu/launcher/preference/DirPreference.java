package cn.qemu.launcher.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;
import android.support.v7.preference.*;
import android.support.v4.content.res.TypedArrayUtils;

/**
 * A {@link Preference} that allows for string
 * input.
 * <p>
 * It is a subclass of {@link DialogPreference} and shows the {@link EditText}
 * in a dialog.
 * <p>
 * This preference will store a string into the SharedPreferences.
 */
public class DirPreference extends DialogPreference {
    private String mText;

    public DirPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public DirPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,defStyleAttr);
    }

    public DirPreference(Context context, AttributeSet attrs) {
        this(context,attrs,TypedArrayUtils.getAttr(context,cn.qemu.launcher.R.attr.dialogPreferenceStyle,
			android.R.attr.dialogPreferenceStyle));
    }

    public DirPreference(Context context) {
        this(context, null);
    }

    /**
     * Saves the text to the {@link android.content.SharedPreferences}.
     *
     * @param text The text to save
     */
    public void setText(String text) {
        final boolean wasBlocking = shouldDisableDependents();

        mText = text;

        persistString(text);

        final boolean isBlocking = shouldDisableDependents();
        if (isBlocking != wasBlocking) {
            notifyDependencyChange(isBlocking);
        }
    }

    /**
     * Gets the text from the {@link android.content.SharedPreferences}.
     *
     * @return The current preference value.
     */
    public String getText() {
        return mText;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

	@Override public int getDialogLayoutResource(){
		return cn.qemu.launcher.R.layout.select_dir;
	}

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setText(restoreValue ? getPersistedString(mText) : (String) defaultValue);
    }
/*
    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }*/

}
