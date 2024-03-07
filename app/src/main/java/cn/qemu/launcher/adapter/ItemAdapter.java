package cn.qemu.launcher.adapter;
import android.view.*;
import java.util.*;
import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;
import android.support.v7.widget.PopupMenu;
import cn.qemu.launcher.R;
import cn.qemu.launcher.activity.*;
import android.content.*;
import cn.qemu.launcher.utils.*;
import java.io.*;
import org.zhoutz.utils.*;
import java.util.concurrent.*;
import android.support.v7.app.*;
import android.util.Log;
import cn.qemu.launcher.application.*;
import cn.qemu.launcher.fragment.*;
import java.lang.reflect.*;

public class ItemAdapter
	extends BaseAdapter
	implements View.OnClickListener, View.OnLongClickListener
{
	private LayoutInflater mInflater;
    private List<Item> mlist;
	MainActivity ma;
	MainFragment mf;
	boolean is_su = false;
	String sh_path, bios_path;

    public ItemAdapter(MainFragment mf, List<Item> list) {
		ma = (MainActivity)mf.getActivity();
		SharedPreferences sp = ma.getSharedPreferences(MainApplication.PREF_SETTINGS, MainApplication.MODE_PRIVATE);
		is_su = sp.getBoolean("is_su", false);
		sh_path = sp.getString("sh_path", "/sdcard/Qemu");
		bios_path = sp.getString("bios_path", ma.getFilesDir().getAbsolutePath()+"/share/qemu");
        mInflater = mf.getLayoutInflater();
        mlist = list;
		this.mf = mf;
    }

    @Override
	public int getCount() {
        return mlist.size();
    }

    @Override
	public Item getItem(int position) {
        return mlist.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ITTHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.conf_item, null);
            holder = new ITTHolder();
            holder.t1 = convertView.findViewById(R.id.confName);
            holder.t2 = convertView.findViewById(R.id.confDate);
			holder.i = convertView.findViewById(R.id.confGo);
			holder.i.setTag(position);
            convertView.setTag(holder);
        } else
            holder = (ITTHolder)convertView.getTag();
        holder.t1.setText(mlist.get(position).title);
        holder.t2.setText(mlist.get(position).sub);
		convertView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent it = new Intent(ma, EditActivity.class)
				.putExtra("conf_name", mlist.get(position).title);
				ma.startActivityForResult(it, 1);
			}
		});
		convertView.setOnLongClickListener(this);
		holder.i.setOnClickListener(this);
        return convertView;
    }

	@Override
	public void onClick(final View v) {
		final StringBuffer pth = new StringBuffer(sh_path);
		if (pth.charAt(pth.length()-1)!='/')
			pth.append('/');
		final Item mIt = mlist.get((int)v.getTag());
		final String path = pth.append(mIt.title).toString();
		if (mIt.pid == -1) {
			new Thread() {
				public void run() {
					StringBuffer cmd = new StringBuffer("$PREF ").append(ACode.fr(path).trim());
					if (cmd.indexOf(" -L ") == -1)
						cmd.append(" -L ").append(bios_path);
					Log.e("run-qemu", cmd.toString());
					try {
						String cmds = cmd.toString();
						Process mp = is_su?VMR.execsu(cmds):VMR.exec(cmds);
						Thread.sleep(1000L);
						InputStream mip = mp.getInputStream();
						byte[] mB = new byte[mip.available()];
						mip.read(mB);
						String mpid = new String(mB).trim();
						Log.e("run-qemu input", mpid);
						mip = mp.getErrorStream();
						mB = new byte[mip.available()];
						mip.read(mB);
						Log.e("run-qemu error", new String(mB));
						try {
							mp.exitValue();
							ma.runOnUiThread(new Runnable() {
								public void run() {
									new AlertDialog.Builder(ma)
										.setIcon(R.drawable.ic_warning_amber_24dp)
										.setTitle(R.string.run_err)
										.setMessage("运行结束或配置出错，虚拟机已停止运行")
										.setNegativeButton(android.R.string.cancel,null)
										.setPositiveButton(R.string.view_log, new DialogInterface.OnClickListener(){
											public void onClick(DialogInterface di, int p) {
												mf.viewLog();
											}
										})
										.create().show();
								}});
						} catch (IllegalThreadStateException ite) {
							ma.runOnUiThread(new Runnable(){
									public void run() {
										ma.in.tw(ma.getText(R.string.running),Toast.LENGTH_SHORT,ma.getResources().getColor(R.color.green));
										((android.support.v7.widget.AppCompatImageView)v).setImageResource(R.drawable.ic_stop_black_24dp);
									}});
							Field fp = mp.getClass().getDeclaredField("pid");
							fp.setAccessible(true);
							mIt.process = mp;
							mIt.pid = getPID(fp.getInt(mp));
							Log.e("QEMU_PID", String.valueOf(mIt.pid));
						}
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		} else {
			android.os.Process.sendSignal(mIt.pid, android.os.Process.SIGNAL_QUIT);
			try {
				mIt.process.waitFor();
			} catch (InterruptedException i) {
				i.printStackTrace();
			}
			mIt.process.destroyForcibly();
			if (!mIt.process.isAlive()) {
				ma.in.tw(ma.getText(R.string.vm_stopped),Toast.LENGTH_SHORT,ma.getResources().getColor(R.color.green));
				((android.support.v7.widget.AppCompatImageView)v).setImageResource(R.drawable.ic_play_arrow_black_24dp);
				mIt.process = null;
				mIt.pid = -1;
			} else ma.in.tw(ma.getText(R.string.vm_unstopped),Toast.LENGTH_SHORT,ma.getResources().getColor(R.color.red));
		}
	}

	@Override
	public boolean onLongClick(View v)
	{
		PopupMenu pm = new PopupMenu(ma, v, Gravity.END);
		Menu m = pm.getMenu();
		ITTHolder vh = (ITTHolder)v.getTag();
		CharSequence name = vh.t1.getText();
		int pos = vh.i.getTag();
		m.add(R.string.delete)
		.setOnMenuItemClickListener(new ItemClick(pos, name));
		pm.show();
		return true;
	}

	private class ItemClick
	implements MenuItem.OnMenuItemClickListener, View.OnClickListener
	{
		int pos;
		CharSequence name;
	
		public ItemClick(int pos, CharSequence name) {
			this.pos = pos;
			this.name = name;
		}

		public void onClick(View p1) {
			mf.viewLog();
		}

		public boolean onMenuItemClick(MenuItem p1) {
			if (new File(ma.getShPath()+"/"+name).delete()) {
				mlist.remove(pos);
				notifyDataSetChanged();
			} else
				ma.showSnack(
					ma.getText(R.string.operation_error),
					ma.getText(R.string.view_log),
					this);
			return true;
		}
	}

	int getPID(int ppid) throws IOException, InterruptedException {
		String cmd = String.format("ps -A -o PID -P %d", ppid);
		Process p = is_su?VMR.execsu(cmd):VMR.exec(cmd);
		p.waitFor();
		InputStream is = p.getInputStream();
		Scanner s = new Scanner(is);
		s.next();
		return s.nextInt();
	}
}
