package cn.qemu.launcher.fragment;
import android.support.v4.app.*;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.os.Bundle;
import cn.qemu.launcher.preference.DirPreference;
import cn.qemu.launcher.preference.DirPreferenceDialogFragment;
import android.support.v7.preference.ListPreference;
import cn.qemu.launcher.utils.VMR;
import cn.qemu.launcher.activity.MainActivity;
import android.content.res.Configuration;
import android.content.SharedPreferences;
import android.support.v7.preference.*;
import android.support.v14.preference.*;
import java.util.*;
import android.content.*;
import cn.qemu.launcher.preference.*;
import cn.qemu.launcher.activity.*;

public class ConfFragment
	extends PreferenceFragmentCompat
	implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener
{
	VMR v = new VMR();
	DiskPreference dip;

	private Preference.OnPreferenceClickListener dic = new Preference.OnPreferenceClickListener() {
		@Override
		public boolean onPreferenceClick(Preference p1) {
			dip = (DiskPreference)p1;
			Intent it = new Intent(getActivity(), FileActivity.class)
				.putExtra(FileActivity.FDF, dip.getFDF())
				.putExtra(FileActivity.PATH, dip.getPath());
			startActivityForResult(it, 1);
			return true;
		}
	};

	@Override
	public void onCreatePreferences(Bundle p1, String p2) {
		getPreferenceManager().setSharedPreferencesName("configuration");
		setPreferencesFromResource(cn.qemu.launcher.R.xml.configuration, p2);
		v.initAll(getActivity());
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
		ListPreference arch=(ListPreference)findPreference("qemu_system");
		arch.setEntries(v.getArchitecture());
		arch.setEntryValues(v.getArchitecture());
		final ListPreference cpu=(ListPreference)findPreference("cpu");
		cpu.setEntries(v.getCPUMode(arch.getValue()));
		cpu.setEntryValues(v.getCPUMode(arch.getValue()));
		final ListPreference machine=(ListPreference)findPreference("machine");
		machine.setEntries(v.getMachine(arch.getValue()));
		machine.setEntryValues(v.getMachine(arch.getValue()));
		ListPreference gprf = (ListPreference)findPreference("graphics");
		gprf.setOnPreferenceChangeListener(new ChangeEnable("port","sdl"));
		findPreference("port").setEnabled(!"sdl".equals(gprf.getValue()));
		for (String ad:new String[]{"hda","hdb","hdc","hdd","fda","fdb","cdrom","kernel","initrd"})
			findPreference(ad).setOnPreferenceClickListener(dic);
		ListPreference vga=(ListPreference)findPreference("vga");
		vga.setEntries(v.getVGA());
		vga.setEntryValues(v.getVGA());
		MultiSelectListPreference soundhw=(MultiSelectListPreference)findPreference("soundhw");
		soundhw.setEntries(v.getSoundhw(arch.getValue()));
		soundhw.setEntryValues(v.getSoundhw(arch.getValue()));
		findPreference("net_type").setOnPreferenceChangeListener(new ChangeEnable("net_card", "none"));
		Preference netc = findPreference("net_card");
		netc.setEnabled(!"none".equals(v));
		netc.setOnPreferenceClickListener(this);
		arch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
				@Override
				public boolean onPreferenceChange(Preference p, Object obj) {
					cpu.setEntries(v.getCPUMode((String)obj));
					cpu.setEntryValues(v.getCPUMode((String)obj));
					machine.setEntries(v.getMachine((String)obj));
					machine.setEntryValues(v.getMachine((String)obj));
					return true;
				}
			});
		for (int i=0,l=getPreferenceScreen().getPreferenceCount();i<l;i++) {
			Preference p=getPreferenceScreen().getPreference(i);
			if (p instanceof PreferenceCategory)
				for (int ip = 0, lp = ((PreferenceCategory)p).getPreferenceCount(); ip < lp; ip++) {
					Preference pv=((PreferenceCategory)p).getPreference(ip);
					if (pv instanceof ListPreference)
						pv.setSummary(((ListPreference)pv).getEntry());
					else if (pv instanceof EditTextPreference)
						pv.setSummary(((EditTextPreference)pv).getText());
					else if (pv instanceof DirPreference)
						pv.setSummary(((DirPreference)pv).getText());
					else if (pv instanceof DiskPreference)
						pv.setSummary(((DiskPreference)pv).getPath());
					else if (pv instanceof MultiSelectListPreference) {
						String list=((MultiSelectListPreference)pv).getValues().toString();
						pv.setSummary(list.substring(1, list.length() - 1));
					}
				}
		}
	}

	@Override
	public void onDisplayPreferenceDialog(Preference preference) {
		DialogFragment dialogFragment=null;
        if (preference instanceof DirPreference)
            dialogFragment = DirPreferenceDialogFragment.newInstance(preference.getKey());
		else if ("smp".equals(preference.getKey()))
			dialogFragment = SmpDialogFragment.newInstance(preference.getKey());
        if (dialogFragment != null) {
			dialogFragment.setTargetFragment(this, 0);
			dialogFragment.show(this.getFragmentManager(),
								"android.support.v7.preference.PreferenceFragment.DIALOG");
        } else
			super.onDisplayPreferenceDialog(preference);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (dip != null && resultCode == MainActivity.RESULT_OK)
			dip.setPath(data.getExtras().getString(MainActivity.SELECTEDPATH));
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences p1, String p2) {
		Preference preference = findPreference(p2);
		if (preference instanceof ListPreference)
			preference.setSummary(((ListPreference)preference).getEntry());
		else if (preference instanceof EditTextPreference)
			preference.setSummary(((EditTextPreference)preference).getText());
		else if (preference instanceof DirPreference)
			preference.setSummary(((DirPreference)preference).getText());
		else if (preference instanceof DiskPreference)
			preference.setSummary(((DiskPreference)preference).getPath());
		else if (preference instanceof MultiSelectListPreference) {
			String list=((MultiSelectListPreference)preference).getValues().toString();
			preference.setSummary(list.substring(1, list.length() - 1));
		}
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		Intent in=new Intent(getActivity(), NetCardActivity.class);
		in.putExtra("network_cards", v.getNetworkCard());
		startActivity(in);
		return true;
	}

	class ChangeEnable implements Preference.OnPreferenceChangeListener {
		CharSequence a, b;
		public ChangeEnable(CharSequence a, CharSequence b) {
			this.a=a;
			this.b=b;
		}
		public boolean onPreferenceChange(Preference p, Object v) {
			findPreference(a).setEnabled(!b.equals(v));
			return true;
		}
	}
}
