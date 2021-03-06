package com.pdftron.reactnative.nativeviews;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.pdftron.pdf.controls.AnnotationToolbar;
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment;
import com.pdftron.pdf.dialog.BookmarksDialogFragment;
import com.pdftron.pdf.tools.ToolManager;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;
import com.pdftron.pdf.utils.ViewerUtils;
import com.pdftron.reactnative.R;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.Iterator;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class RNPdfViewCtrlTabFragment extends PdfViewCtrlTabFragment {

    private final String TAG = "ahihi: ";

    @Override
    protected void initLayout() {
        super.initLayout();
        Log.d(TAG, "initLayout: ");
        this.mDownloadDocumentDialog.setMessage("Opening book...");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContentLayout = R.layout.custom_controls_fragment_tabbed_pdfviewctrl_tab_content;
    }

    @Override
    public void openNavigationList(BookmarksDialogFragment bookmarksDialogFragment, int marginTop, int marginBottom) {
        Log.d(TAG, "openNavigationList: ");
        super.openNavigationList(bookmarksDialogFragment, marginTop, marginBottom);
    }

    @Override
    protected void openConvertibleFormats(String tag) {
        // super.openConvertibleFormats(tag);
        Activity activity = this.getActivity();
        if (activity != null) {
            final ProgressDialog progressDialog = new ProgressDialog(activity);
            this.mDisposables.add(this.convertToPdf(tag).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).doOnSubscribe(new Consumer<Disposable>() {
                public void accept(Disposable disposable) throws Exception {
                    progressDialog.setMessage("Opening book...");
                    progressDialog.setCancelable(false);
                    progressDialog.setProgressStyle(0);
                    progressDialog.setIndeterminate(true);
                    progressDialog.show();
                }
            }).subscribe(filePath -> {
                progressDialog.dismiss();
                RNPdfViewCtrlTabFragment.this.mCurrentFile = new File(filePath);
                String oldTabTag = RNPdfViewCtrlTabFragment.this.mTabTag;
                int oldTabSource = RNPdfViewCtrlTabFragment.this.mTabSource;
                RNPdfViewCtrlTabFragment.this.mTabTag = RNPdfViewCtrlTabFragment.this.mCurrentFile.getAbsolutePath();
                RNPdfViewCtrlTabFragment.this.mTabSource = 2;
                RNPdfViewCtrlTabFragment.this.mTabTitle = FilenameUtils.removeExtension((new File(RNPdfViewCtrlTabFragment.this.mTabTag)).getName());
                if ((!RNPdfViewCtrlTabFragment.this.mTabTag.equals(oldTabTag) || RNPdfViewCtrlTabFragment.this.mTabSource != oldTabSource) && RNPdfViewCtrlTabFragment.this.mTabListener != null) {
                    RNPdfViewCtrlTabFragment.this.mTabListener.onTabIdentityChanged(oldTabTag, RNPdfViewCtrlTabFragment.this.mTabTag, RNPdfViewCtrlTabFragment.this.mTabTitle, RNPdfViewCtrlTabFragment.this.mFileExtension, RNPdfViewCtrlTabFragment.this.mTabSource);
                }

                RNPdfViewCtrlTabFragment.this.mToolManager.setReadOnly(false);
                RNPdfViewCtrlTabFragment.this.openLocalFile(filePath);
            }, throwable -> {
                progressDialog.dismiss();
                RNPdfViewCtrlTabFragment.this.handleOpeningDocumentFailed(1);
            }));
        }
    }

    @Override
    public void imageStamperSelected(PointF targetPoint) {
        // in react native, intent must be sent from the activity
        // to be able to receive by the activity
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        this.mImageCreationMode = ToolManager.ToolMode.STAMPER;
        this.mAnnotTargetPoint = targetPoint;
        this.mOutputFileUri = ViewerUtils.openImageIntent(activity);
    }

    @Override
    public void imageSignatureSelected(PointF targetPoint, int targetPage, Long widget) {
        // in react native, intent must be sent from the activity
        // to be able to receive by the activity
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        mImageCreationMode = ToolManager.ToolMode.SIGNATURE;
        mAnnotTargetPoint = targetPoint;
        mAnnotTargetPage = targetPage;
        mTargetWidget = widget;
        mOutputFileUri = ViewerUtils.openImageIntent(activity);

    }

    @Override
    public void attachFileSelected(PointF targetPoint) {
        // in react native, intent must be sent from the activity
        // to be able to receive by the activity
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        mAnnotTargetPoint = targetPoint;
        ViewerUtils.openFileIntent(activity);
    }

    @Override
    public void setBookmarkDialogCurrentTab(int index) {
        super.setBookmarkDialogCurrentTab(index);
    }

    // mới thêm vô đây
    public void openNavigationUIControl() {
        this.mTabListener.onOutlineOptionSelected();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
    }

}
