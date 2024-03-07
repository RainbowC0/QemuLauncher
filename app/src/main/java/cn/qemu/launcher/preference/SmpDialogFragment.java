package cn.qemu.launcher.preference;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Bundle;
import android.view.View;
import android.support.v7.preference.*;
import cn.qemu.launcher.R;
import org.zhoutz.utils.*;
import android.widget.*;
import android.support.annotation.RestrictTo;

public class SmpDialogFragment extends PreferenceDialogFragmentCompat {

    private EditText mEditText,mCores,mThreads;
    private String mText;
	private ACode ad=new ACode(this.getActivity());
	//private Pattern sm=Pattern.compile("(cpus=)?(\\d+)(,cores=(\\d+))?(,threads=(\\d+))?");

    public static SmpDialogFragment newInstance(String key) {
        final SmpDialogFragment
			fragment = new SmpDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditText=(EditText)view.findViewById(android.R.id.edit);
        if (mEditText==null)
            throw new IllegalStateException("Dialog view must contain an TextView with id" +
											" @android:id/edit");
		mCores=(EditText)view.findViewById(R.id.confsmpEditText1);
		CheckBox mC=((CheckBox)view.findViewById(R.id.confsmpCheckBox1));
		mC.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override public void onCheckedChanged(CompoundButton com,boolean bool){
				mCores.setEnabled(bool);
			}
		});
		mThreads=(EditText)view.findViewById(R.id.confsmpEditText2);
		CheckBox mT=((CheckBox)view.findViewById(R.id.confsmpCheckBox2));
		mT.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
			@Override public void onCheckedChanged(CompoundButton com,boolean bool){
				mThreads.setEnabled(bool);
			}
		});
		DialogPreference dig=getPreference();
		if("smp".equals(dig.getKey()))
			mText=((EditTextPreference)dig).getText();
		if(mText!=null){
			String a=mText+",";
			mEditText.setText(a.indexOf("cpus=")==0?ad.sj(a,"cpus=",","):a.substring(0,a.indexOf(",")));
			if(a.indexOf("cores=")>-1){
				mC.setChecked(true);
				mCores.setText(ad.sj(a,"cores=",","));
			}
			if(a.indexOf("threads=")>-1){
				mT.setChecked(true);
				mThreads.setText(ad.sj(a,"threads=",","));
			}
		}
    }
    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @Override protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return true;
    }

    @Override public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value=mEditText.getText().toString()
				+(mCores.isEnabled()&&mCores.getText().length()>0?",cores="+mCores.getText():"")
				+(mThreads.isEnabled()&&mThreads.getText().length()>0?",threads="+mThreads.getText():"");
            if (getPreference().callChangeListener(value))
                ((EditTextPreference)getPreference()).setText(value);
        }
    }
}
