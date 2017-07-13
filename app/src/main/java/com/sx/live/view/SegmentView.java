package com.sx.live.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sx.live.R;


//第一:写xml文件
//第二:确定继承关系
//第三:添加构造
//如果导入包不成功,则用 ctrl+shift+o
public class SegmentView extends LinearLayout implements OnClickListener {

	private TextView tv_left;
	private TextView tv_right;
	private boolean isLeftSelected = true;
	private TextView tv_center;

	public SegmentView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		View.inflate(context, R.layout.view_segment, this);
		tv_left = (TextView) findViewById(R.id.tv_left);
		tv_center = (TextView) findViewById(R.id.tv_center);
		tv_right = (TextView) findViewById(R.id.tv_right);

		tv_left.setOnClickListener(this);
		tv_center.setOnClickListener(this);
		tv_right.setOnClickListener(this);
	}

	public SegmentView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SegmentView(Context context) {
		this(context, null);
	}


	@Override
	public void onClick(View v) {

		if (!v.isSelected()) {
			tv_left.setSelected(v.getId() == R.id.tv_left);
			tv_center.setSelected(v.getId() == R.id.tv_center);
			tv_right.setSelected(v.getId() == R.id.tv_right);
			if (listener != null) {
				if (tv_left.isSelected()) {
					listener.onSegmentChange(0);
				} else if (tv_center.isSelected()) {
					listener.onSegmentChange(1);
				} else {
					listener.onSegmentChange(2);

				}
			}
		}

	}

	private OnSegmentChangeListener listener;

	public void setOnSegmentChangeListener(OnSegmentChangeListener listener) {
		this.listener = listener;
	}

	public void setSegmentSelected(int selectedIndex) {
		switch (selectedIndex) {
			case 0:
				tv_left.setSelected(true);
				break;
			case 1:
				tv_center.setSelected(true);

				break;
			case 2:
				tv_right.setSelected(true);

				break;
		}
		if (listener != null) {
			listener.onSegmentChange(selectedIndex);
		}
	}

	public interface OnSegmentChangeListener {
		void onSegmentChange(int selectedIndex);
	}


}
