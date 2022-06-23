package com.smartiotdevices.iotbox;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.smartiotdevices.iotbox.sshutils.SessionController;

import java.util.Objects;

public class FrgSecurityCam extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    @SuppressLint("StaticFieldLeak")
    static FrgSecurityCam FRG_SECURITYCAM;
    WebChromeClient.CustomViewCallback custom_view_callback, cv_callback;
    FrameLayout frame_layout, fr_layout;
    View root_view, custom_view, c_view;
    WebView web_view;
    SwipeRefreshLayout swipe_layout;
    Button show;
    Runnable fullscreenmode = null;
    Handler handler = null;
    boolean clicked = false;

    public FrgSecurityCam()
    {

    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        root_view = inflater.inflate(R.layout.fragment_securitycam, container, false);
        show = root_view.findViewById(R.id.btn_securitycam_live);
        swipe_layout = root_view.findViewById(R.id.swipe_securitycam);
        swipe_layout.setOnRefreshListener(this);
        swipe_layout.setEnabled(false);
        FRG_SECURITYCAM = this;

        if (clicked)
        {
            if (SessionController.isConnected())
            {
                show.setVisibility(View.GONE);
                showWebView();
            }
        }

        if (getActivity() != null)
        {
            show.setOnClickListener(v ->
            {
                if (!SessionController.isConnected())
                {
                    Toast.makeText(getActivity(), getString(R.string.message_securitycam_fail), Toast.LENGTH_SHORT).show();
                }

                else
                {
                    clicked = true;
                    show.setVisibility(View.GONE);
                    Toast.makeText(getActivity(), getString(R.string.message_loading), Toast.LENGTH_SHORT).show();
                    SessionController.getSessionController().cmdExec(null, null, getString(R.string.cmd_start_securitycam_live));
                    showWebView();
                }

            });
        }

        setHasOptionsMenu(true);
        return root_view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_admin,menu);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    public static FrgSecurityCam getInstance()
    {
        return FRG_SECURITYCAM;
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void showWebView()
    {
        String webUA = "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        swipe_layout.setEnabled(true);
        web_view = root_view.findViewById(R.id.webview_securitycam_viewer);
        web_view.getSettings().setLoadsImagesAutomatically(true);
        web_view.getSettings().setJavaScriptEnabled(true);
        web_view.getSettings().setDomStorageEnabled(true);
        web_view.getSettings().setSupportZoom(true);
        web_view.getSettings().setBuiltInZoomControls(true);
        web_view.getSettings().setDisplayZoomControls(false);
        web_view.getSettings().setAllowContentAccess(true);
        web_view.getSettings().setAllowFileAccess(true);
        web_view.getSettings().setAppCacheEnabled(true);
        web_view.getSettings().setUserAgentString(webUA);
        web_view.getSettings().setUseWideViewPort(true);
        web_view.getSettings().setLoadWithOverviewMode(true);
        web_view.getSettings().setMediaPlaybackRequiresUserGesture(false);
        web_view.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
        web_view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        web_view.loadUrl("http://" + SessionController.getSessionController().getSessionUserInfo().getHost() + getString(R.string.port_address_securitycam));
        web_view.setWebViewClient(new WebViewClient()
        {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon)
            {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                super.onPageFinished(view, url);
            }
        });

        web_view.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public Bitmap getDefaultVideoPoster()
            {
                if (c_view == null)
                {
                    return null;
                }
                return BitmapFactory.decodeResource(getResources(), 2130837573);
            }

            @Override
            public void onShowCustomView(View view, CustomViewCallback callback)
            {
                if (c_view != null)
                {
                    onHideCustomView();
                    return;
                }
                FrgSecurityCam.this.custom_view = view;
                c_view = FrgSecurityCam.this.custom_view;
                if (getActivity() != null)
                {
                    FrgSecurityCam.this.frame_layout = (FrameLayout) getActivity().getWindow().getDecorView();
                    fr_layout = FrgSecurityCam.this.frame_layout;
                    fr_layout.addView(c_view, new ViewGroup.LayoutParams(-1, -1));
                    getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

                    Objects.requireNonNull(getActivity()).getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility ->
                    {
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0)
                        {
                            if (handler == null)
                            {
                                handler = new Handler();
                            }
                            fullscreenmode = () -> Objects.requireNonNull(getActivity()).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                            handler.postDelayed(fullscreenmode, 3000);
                        }
                    });
                }
                ActivityMain.getInstance().toolbar.setVisibility(View.GONE);
                ActivityMain.getInstance().toolbar_tab.setVisibility(View.GONE);
                ActivityMain.getInstance().view_pager.setPagingEnabled(false);
                swipe_layout.setRefreshing(false);
                swipe_layout.setEnabled(false);
                FrgSecurityCam.this.custom_view_callback = callback;
                cv_callback = FrgSecurityCam.this.custom_view_callback;
            }

            @Override
            public void onHideCustomView()
            {
                onHideView();
            }
        });
    }

    @Override
    public void onRefresh()
    {
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(this).attach(this).commit();
        }
        Toast.makeText(getActivity(), getString(R.string.message_loading), Toast.LENGTH_SHORT).show();
        Toast.makeText(getActivity(), getString(R.string.message_loading_live), Toast.LENGTH_LONG).show();
    }

    public void onHideView()
    {
        if (fr_layout != null)
        {
            fr_layout.removeView(c_view);
            c_view = null;
        }
        if (getActivity() !=null)
        {
            getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        }
        ActivityMain.getInstance().toolbar.setVisibility(View.VISIBLE);
        ActivityMain.getInstance().toolbar_tab.setVisibility(View.VISIBLE);
        ActivityMain.getInstance().view_pager.setPagingEnabled(true);
        swipe_layout.setEnabled(true);
        if (handler != null && fullscreenmode != null)
        {
            handler.removeCallbacks(fullscreenmode);
            fullscreenmode = null;
            handler = null;
        }
        if (cv_callback != null)
        {
            cv_callback.onCustomViewHidden();
        }
        cv_callback = null;
    }

    public void reloadFragment()
    {
        FrgSecurityCam frg = this;
        if (getFragmentManager() != null)
        {
            getFragmentManager().beginTransaction().detach(frg).attach(frg).commitNowAllowingStateLoss();
        }
    }
}
