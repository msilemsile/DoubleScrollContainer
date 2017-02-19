# DoubleScrollContainer
@Deprecated see https://github.com/msilemsile/MultiScrollContainer

包含两个view视图，上下翻页~

上下都为普通view  
![Image](https://raw.githubusercontent.com/msilemsile/DoubleScrollContainer/master/example2.gif)  

上下都为可滑动view  
![Image](https://raw.githubusercontent.com/msilemsile/DoubleScrollContainer/master/example1.gif)  

上为滑动view 下为viewpager  
![Image](https://raw.githubusercontent.com/msilemsile/DoubleScrollContainer/master/example3.gif)

```
<com.msile.android.views.DoubleScrollContainer
	android:layout_width="match_parent"
	android:layout_height="match_parent">

	<LinearLayout
		android:layout_width="match_parent"
		android:layout_height="wrap_content"
		android:orientation="vertical">

		//add your TopView !!(id 为 R.id.double_scroll_top_view )

		//add your BottomView !!(id 为 R.id.double_scroll_bottom_view)
		
		//!!PS：如果topview或者bottomview是viewpager 请设置内部可滚动view id为 R.id.double_scroll_inner_scroll	

	</LinearLayout>
</com.msile.android.views.DoubleScrollContainer>
```								
