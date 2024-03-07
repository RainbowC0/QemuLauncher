package cn.qemu.launcher.adapter;
import android.widget.*;
import android.view.*;
import android.content.*;
import java.util.*;
import cn.qemu.launcher.*;
import org.zhoutz.utils.*;

public class NetCardsAdapter extends BaseAdapter{
	private LayoutInflater mInflater;
    private List<String> mlist;
	private Context cot;

    public NetCardsAdapter(Context context, List<String> list) {
        mInflater=LayoutInflater.from(context);
        mlist=list;cot=context;
    }

    @Override public int getCount() {
        return mlist.size();
    }

    @Override public String getItem(int position) {
        return mlist.get(position);
    }

	public void setList(List<String> list){
		mlist=list;
		notifyDataSetChanged();
	}

    @Override public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TTHolder holder = null;
        if (convertView == null){
            convertView = mInflater.inflate(R.layout.netcards_item,null);
            holder = new TTHolder();
            holder.t1 = convertView.findViewById(R.id.netcard_model);
            holder.t2 = convertView.findViewById(R.id.netcard_preference);
            convertView.setTag(holder);
        } else
			holder=(TTHolder)convertView.getTag();
		android.util.Log.i("Netcards", mlist.get(position));
		holder.t1.setText(captureName(ACode.sj(mlist.get(position)+",","model=",",")));
		holder.t2.setText(mlist.get(position));
		return convertView;
    }

	public static String captureName(String value){
		char[] ch = value.toCharArray();
		if(ch[0]>='a'&&ch[0]<='z')
			ch[0]=(char)(ch[0]-32);
		return new String(ch);
	} 
}
