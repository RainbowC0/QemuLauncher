package cn.qemu.launcher.preference;
import android.support.v7.preference.*;
import android.content.*;
import android.util.*;
import android.support.v4.content.res.*;
import android.content.res.*;
import cn.qemu.launcher.activity.*;
import android.view.*;
import android.support.annotation.*;

public class DiskPreference extends Preference{

	String mPath;
	Context mCont;
	View dtb;
	int mFdf;

	public DiskPreference(Context context){
		this(context,null);
	}

	public DiskPreference(Context context,AttributeSet attrs){
		this(context, attrs, TypedArrayUtils.getAttr(context,R.attr.preferenceStyle,android.R.attr.preferenceStyle));
	}

	public DiskPreference(Context context,AttributeSet attrs,int defStyleAttr){
		this(context,attrs,defStyleAttr,0);
	}

	public DiskPreference(Context context,AttributeSet attrs,int defStyleAttr,int defStyleRes){
		super(context,attrs,defStyleAttr,defStyleRes);
		mCont=context;
		TypedArray ta=context.obtainStyledAttributes(attrs,R.styleable.DiskPreference);
		mFdf=ta.getInt(R.styleable.DiskPreference_fdf,0);
		setWidgetLayoutResource(R.layout.pref_disk);
	}

	public void setFDF(int fdf) {
		mFdf=fdf;
	}

	public int getFDF() {
		return mFdf;
	}

	@Nullable
	public void setPath(String path) {
        final boolean wasBlocking=shouldDisableDependents();
        mPath=path;
        persistString(path);
        final boolean isBlocking=shouldDisableDependents();
        if (isBlocking!=wasBlocking)
            notifyDependencyChange(isBlocking);
		if (dtb!=null)
			dtb.setVisibility(path==null||path.isEmpty()?View.GONE:View.VISIBLE);
    }

    public String getPath() {
        return mPath;
    }

	@Override
	public void onBindViewHolder(PreferenceViewHolder holder){
		super.onBindViewHolder(holder);
		dtb=holder.findViewById(R.id.disk_delete);
		dtb.setVisibility(mPath==null||mPath.isEmpty()?View.GONE:View.VISIBLE);
		dtb.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View v){
				setPath(null);
			}
		});
	}

    @Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setPath(restoreValue ? getPersistedString(mPath) : (String) defaultValue);
    }
}
