package com.xmartlabs.daydreaming.ui;

import android.support.annotation.NonNull;

import com.f2prateek.dart.HensonNavigable;

/**
 * Created by scasas on 3/31/17.
 */
@HensonNavigable
public class DashboardActivity extends SingleFragmentActivity {
  @NonNull
  @Override
  protected BaseFragment createFragment() {
    return new DashboardFragmentBuilder().build();
  }
}
