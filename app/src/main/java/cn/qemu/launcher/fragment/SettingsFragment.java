package cn.qemu.launcher.fragment;
import android.support.v7.preference.*;
import android.os.Bundle;
import java.io.*;
import android.support.v4.app.DialogFragment;
import android.view.*;
import android.content.DialogInterface;
import cn.qemu.launcher.adapter.*;
import java.util.ArrayList;
import android.widget.*;
import cn.qemu.launcher.preference.*;
import android.util.*;
import cn.qemu.launcher.application.*;
import cn.qemu.launcher.activity.*;

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener
{
	CheckBoxPreference issu,isdark;
	ItemAdapter it;

	@Override public void onCreatePreferences(Bundle p1, String p2){
		getPreferenceManager().setSharedPreferencesName(MainApplication.PREF_SETTINGS);
		addPreferencesFromResource(cn.qemu.launcher.R.xml.settings_main);
		issu=(CheckBoxPreference)findPreference("is_su");
		isdark=(CheckBoxPreference)findPreference("is_dark");
		EditTextPreference pf = (EditTextPreference)findPreference("logcat_params");
		pf.setOnPreferenceChangeListener(this);
		pf.setSummary(pf.getText());
		DirPreference tmp=(DirPreference)findPreference("sh_path");
		tmp.setOnPreferenceChangeListener(this);
		tmp.setSummary(tmp.getText());
		tmp = (DirPreference)findPreference("bios_path");
		tmp.setOnPreferenceChangeListener(this);
		tmp.setSummary(tmp.getText());
		if (!isRoot()) {
			issu.setChecked(false);
			issu.setEnabled(false);
		}
	}

	@Override public void onDisplayPreferenceDialog(Preference preference){
		DialogFragment dialogFragment=null;
        if (preference instanceof DirPreference)
            dialogFragment = DirPreferenceDialogFragment.newInstance(preference.getKey());
        if (dialogFragment!=null) {
            dialogFragment.setTargetFragment(this, 0);
            dialogFragment.show(this.getFragmentManager(),
			"android.support.v7.preference.PreferenceFragment.DIALOG");
        } else
            super.onDisplayPreferenceDialog(preference);
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object obj){
		pref.setSummary((CharSequence)obj);
		return true;
	}

	public static boolean isRoot(){
		Process process=null;
		try {
			process = Runtime.getRuntime().exec("su -c whoami");
			process.waitFor();
			InputStream ip = process.getInputStream();
			byte[] bs = new byte[ip.available()];
			ip.read(bs);
			ip.close();
			process.destroy();
			String rs = new String(bs).trim();
			Log.i("SettingsFragment",rs);
			return "root".equals(rs);
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
