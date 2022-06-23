package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.smartiotdevices.iotbox.sshutils.SessionController;

import static android.content.Context.DOWNLOAD_SERVICE;

public class FrgVideoList extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public FrgVideoList()
    {

    }

    public static FrgVideoList newInstance(String param1, String param2)
    {
        FrgVideoList fragment = new FrgVideoList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null)
        {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = inflater.inflate(R.layout.fragment_video_list, container, false);
        SwipeRefreshLayout swipeLayout = rootView.findViewById(R.id.swipe_webview);
        swipeLayout.setOnRefreshListener(this);
        String webUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";

        WebView webView = rootView.findViewById(R.id.webview);
        webView.getSettings().setLoadsImagesAutomatically(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setAllowContentAccess(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAppCacheEnabled(true);
        webView.getSettings().setUserAgentString(webUA);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.loadUrl("http://"+SessionController.getSessionController().getSessionUserInfo().getHost()+"/technodynamite.php");
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public Bitmap getDefaultVideoPoster()
            {
                return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
            }
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> downloadDialog(url,userAgent,contentDisposition,mimetype));

        return rootView;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh()
    {
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
        if (!SessionController.isConnected())
        {
            Toast.makeText(getActivity(), getString(R.string.message_webfail), Toast.LENGTH_SHORT).show();
        }

        else
        {
            Toast.makeText(getActivity(), getString(R.string.message_loading), Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnFragmentInteractionListener
    {
        void onFragmentInteraction(Uri uri);
    }

    public void onButtonPressed(Uri uri)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction(uri);
        }
    }

    private void downloadDialog(final String url, final String userAgent, String contentDisposition, String mimetype)
    {
        if (getActivity() != null)
        {
            final String filename = URLUtil.guessFileName(url, contentDisposition, mimetype);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.title_download));
            builder.setMessage(getString(R.string.message_saveconfirm) + filename);
            builder.setPositiveButton(getString(R.string.button_yes), (dialog, which) ->
            {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                String cookie = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("Cookie", cookie);
                request.addRequestHeader("User-Agent", userAgent);
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);
                if (downloadManager != null)
                {
                    downloadManager.enqueue(request);
                }
            });

            builder.setNegativeButton(getString(R.string.button_cancel), (dialog, which) -> dialog.cancel());
            builder.create().show();
        }
    }
}