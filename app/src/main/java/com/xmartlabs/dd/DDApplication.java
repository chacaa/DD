package com.xmartlabs.dd;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.jakewharton.threetenabp.AndroidThreeTen;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.xmartlabs.dd.common.rx.CompletableObserverWithErrorHandling;
import com.xmartlabs.dd.common.rx.FlowableObserverWithErrorHandling;
import com.xmartlabs.dd.common.rx.MaybeObserverWithErrorHandling;
import com.xmartlabs.dd.common.rx.ObserverWithErrorHandling;
import com.xmartlabs.dd.common.rx.SingleObserverWithErrorHandling;
import com.xmartlabs.dd.helper.GeneralErrorHelper;
import com.xmartlabs.dd.module.AndroidModule;

import javax.inject.Inject;

import bullet.ObjectGraph;
import io.fabric.sdk.android.Fabric;
import io.reactivex.plugins.RxJavaPlugins;
import timber.log.Timber;

public class DDApplication extends Application {
  private static DDApplication instance;

  private ObjectGraph bullet;

  @Inject
  GeneralErrorHelper generalErrorHelper;

  public DDApplication() {
    instance = this;
  }

  public static DDApplication getContext() {
    return instance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    initializeThreeTenABP();
    initializeInjections();
    initializeDataBase();
    initializeRxErrorHandler();
    initializeLogging(); // Crashlytics initialization should go at the end.
  }

  private void initializeInjections() {
    ApplicationComponent component = createComponent();
    bullet = createBullet(component);
    inject(this);
  }

  protected ApplicationComponent createComponent() {
    return DaggerApplicationComponent.builder()
        .androidModule(new AndroidModule(this))
        .build();
  }

  protected ObjectGraph createBullet(ApplicationComponent component) {
    return new BulletApplicationComponent(component);
  }

  private void initializeDataBase() {
    FlowManager.init(new FlowConfig.Builder(this).build());
  }

  private void initializeThreeTenABP() {
    AndroidThreeTen.init(this);
  }

  private void initializeLogging() {
    CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
        .disabled(BuildConfig.DEBUG)
        .build();
    Crashlytics crashlytics = new Crashlytics.Builder().core(crashlyticsCore).build();
    Fabric.with(this, crashlytics);

    if (BuildConfig.DEBUG) {
      Timber.plant(new Timber.DebugTree());
    }
    Timber.plant(new CrashlyticsTree());
  }

  public <T> T inject(final T t) {
    return bullet.inject(t);
  }

  @SuppressWarnings("unchecked")
  private void initializeRxErrorHandler() {
    RxJavaPlugins.setOnSingleSubscribe((single, singleObserver) ->
        new SingleObserverWithErrorHandling<>(singleObserver, generalErrorHelper.getGeneralErrorAction()));
    RxJavaPlugins.setOnObservableSubscribe((observable, observer) ->
        new ObserverWithErrorHandling<>(observer, generalErrorHelper.getGeneralErrorAction()));
    RxJavaPlugins.setOnMaybeSubscribe((maybe, maybeObserver) ->
        new MaybeObserverWithErrorHandling<>(maybeObserver, generalErrorHelper.getGeneralErrorAction()));
    RxJavaPlugins.setOnFlowableSubscribe((flowable, subscriber) ->
        new FlowableObserverWithErrorHandling<>(subscriber, generalErrorHelper.getGeneralErrorAction()));
    RxJavaPlugins.setOnCompletableSubscribe((completable, completableObserver) ->
        new CompletableObserverWithErrorHandling(completableObserver, generalErrorHelper.getGeneralErrorAction()));
  }
}