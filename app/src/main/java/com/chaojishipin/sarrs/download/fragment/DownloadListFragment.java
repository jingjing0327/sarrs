package com.chaojishipin.sarrs.download.fragment;import android.app.AlertDialog.Builder;import android.app.Dialog;import android.content.BroadcastReceiver;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.content.IntentFilter;import android.graphics.Color;import android.os.AsyncTask;import android.os.Bundle;import android.os.Handler;import android.os.Message;import android.support.v4.app.Fragment;import android.text.Spannable;import android.text.SpannableStringBuilder;import android.text.TextUtils;import android.text.style.BackgroundColorSpan;import android.text.style.ForegroundColorSpan;import android.util.Log;import android.util.SparseArray;import android.view.LayoutInflater;import android.view.View;import android.view.View.OnClickListener;import android.view.ViewGroup;import android.widget.AdapterView;import android.widget.AdapterView.OnItemClickListener;import android.widget.AdapterView.OnItemLongClickListener;import android.widget.RelativeLayout;import android.widget.TextView;import android.widget.Toast;import com.chaojishipin.sarrs.ChaoJiShiPinApplication;import com.chaojishipin.sarrs.R;import com.chaojishipin.sarrs.download.download.DownloadUtils;import com.chaojishipin.sarrs.utils.DataUtils;import com.mylib.download.DownloadUtil;import com.mylib.download.activity.DownloadJobActivity;import com.chaojishipin.sarrs.download.adapter.DownloadFolderAdapter;import com.chaojishipin.sarrs.download.download.Constants;import com.chaojishipin.sarrs.download.download.ContainSizeManager;import com.chaojishipin.sarrs.download.download.DownloadFolderJob;import com.chaojishipin.sarrs.download.download.DownloadHelper;import com.chaojishipin.sarrs.download.download.DownloadJob;import com.chaojishipin.sarrs.fragment.ChaoJiShiPinBaseFragment;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenu;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenuAdapter;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenuCreator;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenuItem;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenuLayout;import com.chaojishipin.sarrs.thirdparty.swipemenulistview.SwipeMenuListView;import com.chaojishipin.sarrs.utils.ToastUtil;import com.chaojishipin.sarrs.utils.Utils;import java.io.File;import java.util.ArrayList;import java.util.Timer;import java.util.TimerTask;import java.util.concurrent.atomic.AtomicBoolean;public class DownloadListFragment extends ChaoJiShiPinBaseFragment implements OnClickListener,        OnItemClickListener, OnItemLongClickListener, DownloadUtil.DownloadEndListener {    private final static String TAG = "DownloadListFragment";    //    private GestureOverlayView mGesture;    private ViewGroup rootView;    public SwipeMenuListView mListView;    //    private ViewFlipper mViewFlipper;    public DownloadFolderAdapter adapter;    private Dialog mTipDialog;    private RelativeLayout mDowning_folder;    private TextView mRate_text;    private TextView mDowningJobsCount;    public TextView mUserDeletecount;    public TextView mConfirm_delete;    public DownloadFragment downloadFragment;    public RelativeLayout mBottomlayout;    public static int mCurrItem = 0;    private RelativeLayout download_no_item;    private AtomicBoolean mStart = new AtomicBoolean(false);    private Timer mTimer;    private int mLast = 0;    private SparseArray<DownloadFolderJob> mJobs;    private DownloadUtil mUtil;    @Override    public View onCreateView(LayoutInflater inflater, ViewGroup container,                             Bundle savedInstanceState) {        rootView = (ViewGroup) inflater.inflate(                R.layout.download, container, false);        mBottomlayout = (RelativeLayout) rootView.findViewById(R.id.top_edit_layout);        mUserDeletecount = (TextView) rootView.findViewById(R.id.all_select);        mUserDeletecount.setOnClickListener(this);        mConfirm_delete = (TextView) rootView.findViewById(R.id.confirm_delete);        mConfirm_delete.setOnClickListener(this);        mUtil = new DownloadUtil(this.getActivity(), null);        return rootView;    }    private void startTimer(){        if(mStart.get())            return;        mStart.set(true);        TimerTask task = new MyTimerTask();        mTimer = new Timer();        mTimer.schedule(task, 1000, 1000);    }    class MyTimerTask extends TimerTask {        @Override        public void run() {            if(adapter == null)                return;            if(adapter.isDeleteState())                return;            int num = DataUtils.getInstance().getDownloadingJobNum();            if(num == 0 && mLast == 0)                return;            mLast = num;            mHandler.sendEmptyMessage(Constants.MESSAGE_UPDATE_UI);        }    }    @Override    public void onDestroy(){        super.onDestroy();        if(mTimer != null)            mTimer.cancel();        mTimer = null;        if(mUtil != null)            mUtil.destroy();    }    @Override    protected void handleInfo(Message msg) {        switch (msg.what) {            case Constants.MESSAGE_DELETE_DOWNLOAD_FILE:                cancelLoadingView();                cancelDelete(false);                ToastUtil.toastPrompt(getActivity(), R.string.delete_success, 0);                showAvailableSpace();                mListView.setIsOpenStatus(true);                reloadList();                break;            case Constants.MESSAGE_UPDATE_UI:                updateListView();                break;            default:                break;        }    }    public void cancelDeleteAll(boolean isAll) {        if (isAll) {            mUserDeletecount.setText(R.string.check_all);        } else {            mUserDeletecount.setText(R.string.deselect_all);        }    }    @Override    public void onActivityCreated(Bundle savedInstanceState) {        super.onActivityCreated(savedInstanceState);        mDowning_folder = (RelativeLayout) getActivity().findViewById(R.id.item_downing_folder);//      mDowning_folder.setVisibility(View.VISIBLE);        mRate_text = (TextView) getActivity().findViewById(R.id.rate_text);        mDowningJobsCount = (TextView) getActivity().findViewById(R.id.downing_folder_count);        updateListView();        if (null != rootView) {            download_no_item = (RelativeLayout) rootView.findViewById(R.id.download_no_item);            mListView = (SwipeMenuListView) rootView.findViewById(R.id.DownloadListView);//          step 1. create a MenuCreator            SwipeMenuCreator creator = new SwipeMenuCreator() {                @Override                public void create(SwipeMenu menu) {                    // create "delete" item                    SwipeMenuItem deleteItem = new SwipeMenuItem(                            getActivity());                    // set item background                    deleteItem.setBackground(R.drawable.download_swipe_menu_selector);                    // set item width                    deleteItem.setWidth(Utils.dip2px(75));                    // set a icon                    deleteItem.setIcon(R.drawable.delete_normal);                    deleteItem.setTitleColor(Color.WHITE);                    deleteItem.setTitleSize(12);                    deleteItem.setTitle(getActivity().getString(R.string.delete_up));                    // add to menu                    menu.addMenuItem(deleteItem);                }            };            // set creator            mListView.setMenuCreator(creator);            // step 2. listener item click event            mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {                @Override                public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {                    executeDeleteOneItem(position);                    return false;                }            });            // set SwipeListener            mListView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {                @Override                public void onSwipeStart(int position) {                    // swipe start                }                @Override                public void onSwipeEnd(int position) {                    // swipe end                    checkSwipeStatus();                }            });            mListView.setOnItemClickListener(this);            // test item long click            mListView.setOnItemLongClickListener(this);        }        mDowning_folder.setOnClickListener(this);    }    private void checkSwipeStatus() {        SwipeMenuLayout mSwipe = null;        mSwipe = mListView.getSwipeMenuLayout();        if (mSwipe != null) {            if (mSwipe.isOpen()) {                downloadFragment.changeDeleteIconText(getResources().getString(R.string.done));            } else                downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));        } else            downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));    }    @Override    public void onPause() {        if(mTimer != null) {            mTimer.cancel();            mTimer = null;            mStart.set(false);        }        super.onPause();    }    @Override    public void onResume() {        super.onResume();        cancelDeleteAll(false); //4        if (adapter != null && adapter.isDeleteState()                && adapter.getCount() > 0) {            handleBackOnDelete();        }        updateView();        startTimer();    }    @Override    public void onDownloadEnd(DownloadJob job) {        reloadList();        showAvailableSpace();    }    private void updateListView() {        int num = updateDownloadingFolder();        if(num > 0)            updateDownLoadingFolderStatus();    }    private void updateView() {        reloadList();        updateListView();        showAvailableSpace();    }    private void reloadList() {        if(mJobs == null)            mJobs = DataUtils.getInstance().getFolderJobs();        setupListView();        SwipeMenuAdapter swipeAdapter = (SwipeMenuAdapter) mListView.getAdapter();        if (swipeAdapter != null)            adapter = (DownloadFolderAdapter) swipeAdapter.getWrappedAdapter();        if (swipeAdapter != null && adapter != null && mJobs != null) {            adapter.notifyDataSetChanged();        }else if(adapter == null && mJobs != null){            adapter = new DownloadFolderAdapter(mJobs, this.getActivity());            adapter.fragment = downloadFragment;            mListView.setAdapter(adapter);        }        if (downloadFragment != null) {            if (null != mJobs && mJobs.size() > 0) {                downloadFragment.setFilterButtonState(true);            } else {                downloadFragment.setFilterButtonState(false);            }        }    }    /**     * 判断是否有下载中任务，更新下载中文件夹的显隐     */    private int updateDownloadingFolder() {        int num = DataUtils.getInstance().getDownloadJobNum();        if (num > 0) {            if (adapter != null && adapter.isDeleteState())                mDowning_folder.setVisibility(View.GONE);            else                mDowning_folder.setVisibility(View.VISIBLE);        } else {            mDowning_folder.setVisibility(View.GONE);        }        return num;    }    /**     * 更新下载文件夹的状态     */    private void updateDownLoadingFolderStatus() {        int num = DataUtils.getInstance().getDownloadJobNum();        ArrayList<DownloadJob> jobsList = DataUtils.getInstance().getDownloadingJobs();        long totalRate = 0;        boolean isAllPause = true;        for (DownloadJob job : jobsList) {            totalRate += job.getByteRate();            if (isAllPause && job.getStatus() == DownloadJob.DOWNLOADING) {                isAllPause = false;            }        }        if (isAllPause) {            mRate_text.setText("0KB/s");        } else {            mRate_text.setText(DownloadUtils.getDownloadedSpeed(totalRate));        }        String str ="(" + num + ")";        SpannableStringBuilder style=new SpannableStringBuilder(str);        style.setSpan(new ForegroundColorSpan(ChaoJiShiPinApplication.getInstatnce().getResources().getColor(R.color.color_FF1E27)),0,str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);        mDowningJobsCount.setText(style);    }    private void setupListView() {        int num = DataUtils.getInstance().getAllDownloadJobNum();        if (num > 0) {            mListView.setVisibility(View.VISIBLE);            download_no_item.setVisibility(View.GONE);        } else {            mListView.setVisibility(View.GONE);            download_no_item.setVisibility(View.VISIBLE);        }    }    @Override    public boolean onItemLongClick(AdapterView<?> parent, View view,                                   int position, long id) {        return true;    }    @Override    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {        if (adapter.isDeleteState()) {            adapter.setDeleteItem(position, ((SwipeMenuLayout) view).getContentView());        } else {            DownloadFolderJob floderJob = mJobs.valueAt(position);            if (floderJob.getDownloadJobs().size() == 1) {                DownloadJob job = floderJob.getDownloadJobs().valueAt(0);                String path = DownloadHelper.getAbsolutePath(job.getEntity(), job                        .getEntity().getPath());                if(TextUtils.isEmpty(path) || DownloadHelper.getDownloadedFileSize(                        job.getEntity(), job.getEntity().getPath()) == 0){                    Toast.makeText(getActivity(),                            R.string.file_has_been_removed, Toast.LENGTH_SHORT)                            .show();                    return;                }                DataUtils.getInstance().setIfWatch(job.getEntity(), "true");                View v = view.findViewById(R.id.ifwatch);                if(v != null)                    v.setVisibility(View.GONE);                DataUtils.getInstance().playVideo(getActivity(), job);            } else {                // 点击下载剧集列表，当多集时点击跳到下载专辑信息界面                Intent intent = new Intent(getActivity(), DownloadJobActivity.class);                intent.putExtra("index", position);                intent.putExtra("mediaName", floderJob.getMediaName());                intent.putExtra("mediaId", floderJob.getMediaId());                startActivity(intent);            }        }    }    public void updateDeleteView(View view) {        if (mListView == null) {            return;        }        if (adapter == null) {            adapter = (DownloadFolderAdapter) mListView.getAdapter();            adapter.fragment = downloadFragment;        }        if (adapter == null)            return;        if (adapter.isDeleteState()) {            mListView.setIsOpenStatus(true);            // 切回原来状态            downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));            view.setVisibility(View.GONE);            adapter.setDeleteState(false);            updateDownloadingFolder();            adapter.deletedNum = 0;            setSelectAll(false);        } else {            // 切到编辑状态            mListView.setIsOpenStatus(false);            downloadFragment.changeDeleteIconText(getResources().getString(R.string.done));            mDowning_folder.setVisibility(View.GONE);            view.setVisibility(View.VISIBLE);            adapter.setDeleteState(true);        }        adapter.notifyDataSetChanged();    }    public void updateConfirmDeleteView(View view) {        if (mListView == null) {            return;        }        if (adapter == null) {            adapter = (DownloadFolderAdapter) mListView.getAdapter();            adapter.fragment = downloadFragment;        }        if (adapter == null)            return;        if (adapter.isDeleteState() && adapter.deletedNum > 0) {            deleteTip(view);        }    }    public void handleBackOnDelete() {        if (adapter != null && adapter.isDeleteState() && mJobs != null) {            adapter.setDeleteState(false);            adapter.deletedNum = 0;            setSelectAll(false);            adapter.notifyDataSetChanged();        }    }    // 全选    public void selectDownloadVideo() {        if (null == adapter || mJobs == null)            return;        // 如果用户点击了全选按钮        if (adapter.deletedNum != DataUtils.getInstance().getRemainNum()) {            adapter.deletedNum = DataUtils.getInstance().getRemainNum();            String content = getString(R.string.delete_up);            if (null != mConfirm_delete)                mConfirm_delete.setText(content);            mConfirm_delete.setTextColor(getResources().getColor(R.color.color_FF1E27));            setSelectAll(true);        } else {            // 如果用户点击了取消全选按钮            if (null != mConfirm_delete)                mConfirm_delete.setText(R.string.delete_up);            mConfirm_delete.setTextColor(getResources().getColor(R.color.all_select));            // 用户选择取消全选则用户当前选中项为0            adapter.deletedNum = 0;            setSelectAll(false);        }        adapter.notifyDataSetChanged();    }    private void deleteTip(final View view) {        try {            Builder builder = new Builder(getActivity());            builder.setTitle(R.string.tip);            int deletingCount = adapter.deletedNum;            String deleteTipContent = getString(R.string.delete_up)                    + deletingCount                    + getString(R.string.delete_below);            builder.setMessage(deleteTipContent);            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {                @Override                public void onClick(DialogInterface dialog, int which) {                    dialog.dismiss();                    downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));                    deleteDownloadfile();                    view.setVisibility(View.GONE);                }            });            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {                @Override                public void onClick(DialogInterface dialog, int which) {                    dialog.dismiss();                }            });            Dialog dialog = builder.create();            dialog.show();        } catch (Exception e) {            e.printStackTrace();        }    }    private void showAvailableSpace() {        if (ContainSizeManager.getInstance() != null) {            ContainSizeManager.getInstance().setView(this.getActivity());            ContainSizeManager.getInstance().ansynHandlerSdcardSize();        }    }    private void deleteDownloadfile() {        showLoadingView(getActivity(), false, R.string.deleting);		new Thread() {			@Override			public void run() {                executeDelete();                mHandler.sendEmptyMessage(Constants.MESSAGE_DELETE_DOWNLOAD_FILE);			}		}.start();    }    private void setSelectAll(boolean bo){        if(mJobs == null)            return;        int size = mJobs.size();        for(int i=0; i<size; i++)            mJobs.valueAt(i).setCheck(bo);    }    private void cancelDelete(boolean deleteState) {        adapter.setDeleteState(deleteState);        updateDeleteIcon();        adapter.deletedNum = 0;        if (null != adapter) {            setSelectAll(false);        }    }    private void executeDelete() {        if(mJobs == null)            return;        int size = mJobs.size();        ArrayList<Integer> list = new ArrayList<>();        for(int i=0; i<size; i++){            DownloadFolderJob fj = mJobs.valueAt(i);            deleteFolderJob(fj, mJobs.keyAt(i), list);        }        for(Integer key : list)            mJobs.remove(key);    }    private void deleteFolderJob(DownloadFolderJob fj, int key, ArrayList<Integer> list){        if(!fj.isCheck())            return;        list.add(key);        SparseArray<DownloadJob> jobs = fj.getDownloadJobs();        int len = jobs.size();        for(int j=0; j<len; j++)            DataUtils.getInstance().deleteDownloadFile(jobs.valueAt(j));    }    private void executeDeleteOneItem(final int position) {        showLoadingView(getActivity(), false, R.string.deleting);        new Thread(){            public void run(){                DownloadFolderJob folderJob = mJobs.valueAt(position);                folderJob.setCheck(true);                ArrayList<Integer> list = new ArrayList<>();                deleteFolderJob(folderJob, mJobs.keyAt(position), list);                for(Integer key : list)                    mJobs.remove(key);                mHandler.post(new Runnable() {                    public void run() {                        downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));                    }                });                mHandler.sendEmptyMessage(Constants.MESSAGE_DELETE_DOWNLOAD_FILE);            }        }.start();    }    private void showLoadingView(Context context, boolean isCouldCancel, int strId) {        mTipDialog = new Dialog(context, R.style.waiting);        mTipDialog.setContentView(R.layout.dialog_waiting);        TextView textView = (TextView) mTipDialog.findViewById(R.id.waiting_text);        textView.setText(strId);        mTipDialog.setCanceledOnTouchOutside(isCouldCancel);        mTipDialog.setCancelable(isCouldCancel);        if (!mTipDialog.isShowing()) {            mTipDialog.show();        }    }    private void cancelLoadingView() {        if (null != mTipDialog && mTipDialog.isShowing()) {            mTipDialog.cancel();        }    }    // 设置我的下载界面 编辑按钮显示    private void updateDeleteIcon() {        if (DataUtils.getInstance().getCompletedDownloads().size() > 0) {            downloadFragment.setFilterButtonState(true);        } else {            downloadFragment.setFilterButtonState(false);        }    }    @Override    public void onClick(View v) {        switch (v.getId()) {            case R.id.item_downing_folder:                SwipeMenuLayout mSwipe = null;                mSwipe = mListView.getSwipeMenuLayout();                if (mSwipe!=null && mSwipe.isOpen()){                    mListView.getSwipeMenuLayout().smoothCloseMenu();                    downloadFragment.changeDeleteIconText(getResources().getString(R.string.edit));                    return;                }else {                    Intent intent = new Intent(getActivity(), DownloadJobActivity.class);                    intent.putExtra("index", -1);                    startActivity(intent);                }                break;            case R.id.all_select:                if (mCurrItem == 0) {                    downloadFragment.checkUserSelectAll();                    selectDownloadVideo();                } else if (mCurrItem == 1) {                }                break;            case R.id.confirm_delete:                if (mCurrItem == 0) {                    updateConfirmDeleteView(mBottomlayout);                } else if (mCurrItem == 1) {                }                break;        }    }}