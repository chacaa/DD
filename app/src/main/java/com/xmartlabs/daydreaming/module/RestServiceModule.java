package com.xmartlabs.daydreaming.module;

import android.content.Context;

import com.google.gson.Gson;
import com.xmartlabs.daydreaming.R;
import com.xmartlabs.daydreaming.service.AuthService;
import com.xmartlabs.daydreaming.service.common.ServiceStringConverter;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class RestServiceModule {
  @Provides
  @Singleton
  Retrofit provideRetrofit(Context context, @Named(OkHttpModule.CLIENT_SERVICE) OkHttpClient client,
                           RxJava2CallAdapterFactory rxJavaCallAdapterFactory,
                           GsonConverterFactory gsonConverterFactory,
                           ServiceStringConverter serviceStringConverter) {
    return new Retrofit.Builder()
        .addCallAdapterFactory(rxJavaCallAdapterFactory)
        .addConverterFactory(gsonConverterFactory)
        .addConverterFactory(serviceStringConverter)
        .baseUrl(getBaseUrl(context))
        .client(client)
        .build();
  }

  @Provides
  @Singleton
  RxJava2CallAdapterFactory provideRxJavaCallAdapterFactory() {
    return RxJava2CallAdapterFactory.create();
  }

  @Provides
  @Singleton
  GsonConverterFactory provideGsonConverterFactory(@Named(GsonModule.SERVICE_GSON_NAME) Gson gson) {
    return GsonConverterFactory.create(gson);
  }

  @Provides
  @Singleton
  AuthService provideAuthService(Retrofit retrofit) {
    return retrofit.create(AuthService.class);
  }


  @Provides
  @Singleton
  ServiceStringConverter provideStringConverter() {
    return new ServiceStringConverter();
  }

  protected String getBaseUrl(Context context) {
    return context.getString(R.string.url_service);
  }
}
