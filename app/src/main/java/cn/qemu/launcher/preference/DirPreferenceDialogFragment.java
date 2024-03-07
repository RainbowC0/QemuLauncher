package cn.qemu.launcher.preference;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.view.View;
import android.view.KeyEvent;
import android.widget.TextView;
import android.widget.EditText;
import android.support.v7.preference.*;
import android.widget.ListView;
import cn.qemu.launcher.adapter.*;
import cn.qemu.launcher.R;
import android.widget.HorizontalScrollView;
import android.view.inputmethod.*;
import java.io.*;
import java.util.*;
import org.zhoutz.utils.*;

public class DirPreferenceDialogFragment extends PreferenceDialogFragmentCompat {

    private EditText mEditText;
	private ListView mListView;
	private ListItemAdapter it;
    private CharSequence mText;

    public static DirPreferenceDialogFragment newInstance(String key) {
        final DirPreferenceDialogFragment
                fragment = new DirPreferenceDialogFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        mEditText=view.findViewById(R.id.preference_path);
        if (mEditText==null)
            throw new IllegalStateException("Dialog view must contain an TextView with id" +
                    " @id/preference_path");
		DialogPreference dig=getPreference();
		if(dig instanceof DirPreference)
			mText=((DirPreference)dig).getText();
		if(mText==null)mText="/";
		mEditText.setText(mText);
		mEditText.setSelection(mEditText.getText().length());
		mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener(){
			@Override public boolean onEditorAction(TextView p1,int p2,KeyEvent p3){
				if(p2==EditorInfo.IME_ACTION_GO){
					String path=p1.getText().toString();
					if(new File(path).isDirectory())
						it.setList(getFlist(path));
					else
						mEditText.setError(getText(R.string.path_invalid));
				}
				return true;
			}
		});
		mListView=view.findViewById(android.R.id.list_container);
		it=new ListItemAdapter(getActivity(),getFlist(mText.toString()));
		mListView.setAdapter(it);
		mListView.setOnItemClickListener(new ListView.OnItemClickListener(){
			@Override public void onItemClick(android.widget.AdapterView<?> p,View v,int pos,long pps){
				String st=((Item)p.getAdapter().getItem(pos)).title;
				String pat=mEditText.getText().toString();
				if(pos==0&&"..".equals(st))
					pat=new File(pat).getParent();
				else
					pat+=pat.endsWith("/")?st:"/"+st;
				mEditText.setText(pat);
				mEditText.setSelection(pat.length());
				it.setList(getFlist(pat));
			}
		});
    }

    private DirPreference getDirPreference() {
        return (DirPreference) getPreference();
    }

	private ArrayList<Item> getFlist(String pat){
        ArrayList<Item> arrayList=new ArrayList<Item>();
		Item it;
		if(!"/".equals(pat)){
			it=new Item();
			it.icon=R.drawable.ic_folder_black_24dp;
			it.title="..";
			arrayList.add(it);
		}
		File fpa=new File(pat);
		if(!fpa.canRead())
			return arrayList;
		File[] fl=fpa.listFiles(ActivityUtil.DIRFILTER);
		Arrays.sort(fl,new Comparator<File>(){
			public int compare(File a,File b){
				return a.getName().compareToIgnoreCase(b.getName());
			}
		});
        for(File i:fl){
            it=new Item();
            it.icon=R.drawable.ic_folder_black_24dp;
            it.title=i.getName();
            arrayList.add(it);
        }
        return arrayList;
    }

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @Override protected boolean needInputMethod() {
        // We want the input method to show, if possible, when dialog is displayed
        return false;
    }

    @Override public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String value=mEditText.getText().toString();
            if (getDirPreference().callChangeListener(value))
                getDirPreference().setText(value);
        }
    }
}
