package com.chaojishipin.sarrs.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.chaojishipin.sarrs.R;
import com.chaojishipin.sarrs.activity.ChaoJiShiPinRankListDetailActivity;
import com.chaojishipin.sarrs.activity.SearchActivity;
import com.chaojishipin.sarrs.adapter.RankListListViewAdapter;
import com.chaojishipin.sarrs.bean.RankList;
import com.chaojishipin.sarrs.bean.SarrsArrayList;
import com.chaojishipin.sarrs.bean.SlidingMenuLeft;
import com.chaojishipin.sarrs.http.volley.HttpApi;
import com.chaojishipin.sarrs.http.volley.HttpManager;
import com.chaojishipin.sarrs.http.volley.RequestListener;
import com.chaojishipin.sarrs.listener.onRetryListener;
import com.chaojishipin.sarrs.swipe.SwipeMenu;
import com.chaojishipin.sarrs.utils.ConstantUtils;
import com.chaojishipin.sarrs.utils.NetWorkUtils;
import com.chaojishipin.sarrs.widget.NetStateView;
import com.chaojishipin.sarrs.widget.PullToRefreshSwipeListView;
import com.chaojishipin.sarrs.widget.PullToRefreshSwipeMenuListView;
import com.chaojishipin.sarrs.widget.SarrsToast;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.io.ByteArrayOutputStream;

/**
 * create by wulianshu
 */
public class RankListFragment extends MainBaseFragment implements PullToRefreshSwipeListView.OnSwipeListener, PullToRefreshSwipeListView.OnMenuItemClickListener, View.OnClickListener,
        AdapterView.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    private RankListListViewAdapter rankListViewAdapter;

    @Override
    protected void handleInfo(Message msg) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.search_icon:
                buildDrawingCacheAndIntent();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        RankList rankList = (RankList) rankListViewAdapter.getItem(i);
        if (rankList != null) {
            Intent intent = new Intent(this.getActivity(), ChaoJiShiPinRankListDetailActivity.class);
            intent.putExtra("rankList", rankList);
            startActivity(intent);
        }
    }

    @Override
    public void onSwipeStart(int position) {

    }

    @Override
    public void onSwipeEnd(int position) {

    }

    @Override
    public void onMenuItemClick(int position, SwipeMenu menu, int index) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    @Override
    protected void init(){
        mXListView.setMode(PullToRefreshBase.Mode.DISABLED);
        rankListViewAdapter = new RankListListViewAdapter(getActivity(), null);
        mLv.setAdapter(rankListViewAdapter);
        mLv.setOnItemClickListener(this);
        mSearchIcon.setOnClickListener(this);

        getNetData();
    }

    /**
     * 截屏，保存为Bitmap，提供给SearchAvtivity高斯模糊使用
     *
     * @auth daipei
     */
    public void buildDrawingCacheAndIntent() {
        SearchActivity.launch(getActivity(), "");
    }

    public void onEventMainThread(SlidingMenuLeft slidingMenuLeft) {
        if (slidingMenuLeft.getContent_type().equals(ConstantUtils.RANKLIST_CONTENT_TYPE)) {
            getNetData();
        }
    }

    /**
     * 获取网络数据
     */
    @Override
    protected void requestData(){
        requestRankListData(getActivity());
    }

    /**
     * 请求专题数据
     *
     * @paramcid
     */
    private void requestRankListData(Context context) {
        //请求频道页数据
        HttpManager.getInstance().cancelByTag(ConstantUtils.REQUEST_RANKLIST);
        HttpApi.
                getRankListRequest()
                .start(new RequestRanklistListener(), ConstantUtils.REQUEST_RANKLIST);
    }

    private class RequestRanklistListener implements RequestListener<SarrsArrayList> {
        @Override
        public void onResponse(SarrsArrayList result, boolean isCachedData) {
            //进行展现的相关操作
            if (null != result && result.size() > 0) {
                if (null != rankListViewAdapter) {
                    rankListViewAdapter.setmDatas(result);
                    rankListViewAdapter.notifyDataSetChanged();
                } else {
                    rankListViewAdapter = new RankListListViewAdapter(getActivity(), result);
                    mLv.setAdapter(rankListViewAdapter);
                }
            }
            hideErrorView(mRootView);
            mTopToast.setVisibility(View.VISIBLE);
        }

        @Override
        public void netErr(int errorCode) {

        }

        @Override
        public void dataErr(int errorCode) {

        }
    }
}
