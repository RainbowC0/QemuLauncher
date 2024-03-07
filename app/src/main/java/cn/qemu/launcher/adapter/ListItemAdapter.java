package cn.qemu.launcher.adapter;

import android.content.Context;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;
import cn.qemu.launcher.R;

public class ListItemAdapter extends BaseAdapter {
    private LayoutInflater mInflater; //LayoutInflater是用来找layout下xml布局文件，并且实例化
    private List<Item> mlist;

    public ListItemAdapter(Context context,List<Item> list) {
        mInflater=LayoutInflater.from(context); //得到初始化上下文
        mlist=list;
    }

    @Override public int getCount() {
        return mlist.size();
    }

    @Override public Item getItem(int position) {
        return mlist.get(position);
    }

	public void setList(List<Item> list){
		mlist = list;
		notifyDataSetChanged();
	}

    @Override public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ITTHolder holder=null;
        if (convertView==null) {
            convertView = mInflater.inflate(R.layout.file_item,null);  //将布局转换成视图
            holder = new ITTHolder();
            holder.t1 = convertView.findViewById(R.id.fna);
            holder.t2 = convertView.findViewById(R.id.fsz);
            holder.i = convertView.findViewById(R.id.fico);
            convertView.setTag(holder);
        } else
            holder = (ITTHolder)convertView.getTag();
		holder.i.setImageResource(mlist.get(position).icon);
        holder.t1.setText(mlist.get(position).title);
        holder.t2.setText(mlist.get(position).sub);
        return convertView;
    }
}
