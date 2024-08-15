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
import android.support.v7.app.AlertDialog.Builder;
import android.os.Message;

public class ItemAdapter
	extends BaseAdapter
	implements View.OnClickListener, View.OnLongClickListener, Runnable,
	DialogInterface.OnClickListener, MenuItem.OnMenuItemClickListener
{
	private LayoutInflater mInflater;
    private List<Item> mlist;
	MainActivity ma;
	MainFragment mf;
	boolean is_su = false;
	String sh_path, bios_path;

	private Item mIt;
	private String transS;
	private View transV;
	private int transI;

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
				MainActivity a = ma;
				Intent it = new Intent(a, EditActivity.class);
				it.putExtra("conf_name", mlist.get(position).title);
				a.startActivityForResult(it, 1);
			}
		});
		convertView.setOnLongClickListener(this);
		holder.i.setOnClickListener(this);
        return convertView;
    }

	@Override
	public void onClick(View v) {
		StringBuilder pth = new StringBuilder(sh_path);
		if (pth.charAt(pth.length()-1)!='/')
			pth.append('/');
		Item it = mlist.get((int)v.getTag());
		transS = pth.append(it.title).toString();
		if (it.pid == -1) {
			transV = v;
			mIt = it;
			new Thread(this).start();
		} else {
			android.os.Process.sendSignal(it.pid, android.os.Process.SIGNAL_QUIT);
			try {
				it.process.waitFor();
			} catch (InterruptedException i) {
				i.printStackTrace();
			}
			it.process.destroyForcibly();
			MainActivity a = ma;
			ACode in = a.in;
			if (!it.process.isAlive()) {
				in.tw(a.getText(R.string.vm_stopped),Toast.LENGTH_SHORT,a.getResources().getColor(R.color.green));
				((android.support.v7.widget.AppCompatImageView)v).setImageResource(R.drawable.ic_play_arrow_black_24dp);
				it.process = null;
				it.pid = -1;
			} else in.tw(a.getText(R.string.vm_unstopped),Toast.LENGTH_SHORT,a.getResources().getColor(R.color.red));
		}
	}

	public void run() {
		StringBuilder cmd = new StringBuilder("$PREF ").append(ACode.fr(transS).trim());
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
			Log.e("run-qemu", mpid);
			mip = mp.getErrorStream();
			mB = new byte[mip.available()];
			mip.read(mB);
			Log.e("run-qemu", new String(mB));
			Message msg = new Message();
			try {
				mp.exitValue();
				msg.what = MainFragment.HALTED;
				msg.obj = this;
				mf.sendMessage(msg);
			} catch (IllegalThreadStateException ite) {
				msg.what = MainFragment.STARTED;
				msg.obj = transV;
				mf.sendMessage(msg);
				Field fp = mp.getClass().getDeclaredField("pid");
				fp.setAccessible(true);
				mIt.process = mp;
				mIt.pid =  getPID(fp.getInt(mp));
				Log.e("QEMU_PID", String.valueOf(mIt.pid));
			}
		} catch(Exception e) {
			e.printStackTrace();
			Log.e("run-qemu", e.getMessage());
		}
	}

	@Override
	public void onClick(DialogInterface p1, int p2) {
		mf.viewLog();
	}

	@Override
	public boolean onLongClick(View v)
	{
		PopupMenu pm = new PopupMenu(ma, v, Gravity.END);
		Menu m = pm.getMenu();
		ITTHolder vh = (ITTHolder)v.getTag();
		transS = vh.t1.getText().toString();
		transI = vh.i.getTag();
		m.add(R.string.delete)
		.setOnMenuItemClickListener(this);
		pm.show();
		return true;
	}

	public boolean onMenuItemClick(MenuItem p1) {
		MainActivity a = ma;
		if (new File(a.getShPath()+"/"+transS).delete()) {
			mlist.remove(transI);
			notifyDataSetChanged();
		} else
			a.showSnack(
				a.getText(R.string.operation_error),
				a.getText(R.string.view_log),
				this);
		return true;
	}

	private int getPID(int ppid) throws IOException, InterruptedException {
		String cmd = String.format("ps -A -o PID -P %d", ppid);
		Process p = is_su?VMR.execsu(cmd):VMR.exec(cmd);
		p.waitFor();
		InputStream is = p.getInputStream();
		Scanner s = new Scanner(is);
		s.next();
		return s.nextInt();
	}
}
