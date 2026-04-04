package sh.delo.perth;

import android.app.Activity;
import android.app.Service;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import sh.delo.perth.core.data.datastore.UserPreferences;
import sh.delo.perth.core.data.db.PerthDatabase;
import sh.delo.perth.core.data.db.dao.CommandAuditDao;
import sh.delo.perth.core.data.db.dao.SessionDao;
import sh.delo.perth.core.data.repository.CommandAuditRepositoryImpl;
import sh.delo.perth.core.data.repository.SessionRepositoryImpl;
import sh.delo.perth.core.data.repository.SettingsRepositoryImpl;
import sh.delo.perth.core.data.secure.SecureStorage;
import sh.delo.perth.core.di.DatabaseModule_ProvideCommandAuditDaoFactory;
import sh.delo.perth.core.di.DatabaseModule_ProvidePerthDatabaseFactory;
import sh.delo.perth.core.di.DatabaseModule_ProvideSessionDaoFactory;
import sh.delo.perth.core.di.NetworkModule_Companion_ProvideOkHttpClientFactory;
import sh.delo.perth.core.network.SpeechRecognizer;
import sh.delo.perth.core.network.WebSocketZealotTransport;
import sh.delo.perth.feature.command.data.LlmRepositoryImpl;
import sh.delo.perth.feature.command.domain.CommandSafetyGate;
import sh.delo.perth.feature.command.domain.ExecuteCommandUseCase;
import sh.delo.perth.feature.command.domain.InterpretCommandUseCase;
import sh.delo.perth.feature.command.ui.CommandViewModel;
import sh.delo.perth.feature.command.ui.CommandViewModel_HiltModules;
import sh.delo.perth.feature.command.ui.CommandViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import sh.delo.perth.feature.command.ui.CommandViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import sh.delo.perth.feature.session.domain.ConnectToSessionUseCase;
import sh.delo.perth.feature.session.domain.GetSessionsUseCase;
import sh.delo.perth.feature.session.ui.SessionListViewModel;
import sh.delo.perth.feature.session.ui.SessionListViewModel_HiltModules;
import sh.delo.perth.feature.session.ui.SessionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import sh.delo.perth.feature.session.ui.SessionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import sh.delo.perth.feature.settings.ui.SettingsViewModel;
import sh.delo.perth.feature.settings.ui.SettingsViewModel_HiltModules;
import sh.delo.perth.feature.settings.ui.SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import sh.delo.perth.feature.settings.ui.SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import sh.delo.perth.feature.terminal.domain.NavigatePaneUseCase;
import sh.delo.perth.feature.terminal.domain.SendInputUseCase;
import sh.delo.perth.feature.terminal.ui.TerminalViewModel;
import sh.delo.perth.feature.terminal.ui.TerminalViewModel_HiltModules;
import sh.delo.perth.feature.terminal.ui.TerminalViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import sh.delo.perth.feature.terminal.ui.TerminalViewModel_HiltModules_KeyModule_Provide_LazyMapKey;
import sh.delo.perth.feature.voice.data.AndroidSpeechRecognizer;
import sh.delo.perth.feature.voice.data.VoiceRepositoryImpl;
import sh.delo.perth.feature.voice.data.WhisperSpeechRecognizer;
import sh.delo.perth.feature.voice.domain.CaptureVoiceUseCase;
import sh.delo.perth.feature.voice.domain.WriteTaskUseCase;
import sh.delo.perth.feature.voice.ui.VoiceViewModel;
import sh.delo.perth.feature.voice.ui.VoiceViewModel_HiltModules;
import sh.delo.perth.feature.voice.ui.VoiceViewModel_HiltModules_BindsModule_Binds_LazyMapKey;
import sh.delo.perth.feature.voice.ui.VoiceViewModel_HiltModules_KeyModule_Provide_LazyMapKey;

@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class DaggerPerthApp_HiltComponents_SingletonC {
  private DaggerPerthApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public PerthApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements PerthApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements PerthApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements PerthApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements PerthApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements PerthApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements PerthApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements PerthApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public PerthApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends PerthApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends PerthApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends PerthApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends PerthApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(5).put(CommandViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, CommandViewModel_HiltModules.KeyModule.provide()).put(SessionListViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SessionListViewModel_HiltModules.KeyModule.provide()).put(SettingsViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, SettingsViewModel_HiltModules.KeyModule.provide()).put(TerminalViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, TerminalViewModel_HiltModules.KeyModule.provide()).put(VoiceViewModel_HiltModules_KeyModule_Provide_LazyMapKey.lazyClassKeyName, VoiceViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }
  }

  private static final class ViewModelCImpl extends PerthApp_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<CommandViewModel> commandViewModelProvider;

    private Provider<SessionListViewModel> sessionListViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<TerminalViewModel> terminalViewModelProvider;

    private Provider<VoiceViewModel> voiceViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    private InterpretCommandUseCase interpretCommandUseCase() {
      return new InterpretCommandUseCase(singletonCImpl.llmRepositoryImplProvider.get(), singletonCImpl.commandSafetyGateProvider.get());
    }

    private ExecuteCommandUseCase executeCommandUseCase() {
      return new ExecuteCommandUseCase(singletonCImpl.webSocketZealotTransportProvider.get(), singletonCImpl.commandAuditRepositoryImplProvider.get());
    }

    private GetSessionsUseCase getSessionsUseCase() {
      return new GetSessionsUseCase(singletonCImpl.sessionRepositoryImplProvider.get());
    }

    private ConnectToSessionUseCase connectToSessionUseCase() {
      return new ConnectToSessionUseCase(singletonCImpl.sessionRepositoryImplProvider.get());
    }

    private SendInputUseCase sendInputUseCase() {
      return new SendInputUseCase(singletonCImpl.webSocketZealotTransportProvider.get());
    }

    private CaptureVoiceUseCase captureVoiceUseCase() {
      return new CaptureVoiceUseCase(singletonCImpl.voiceRepositoryImplProvider.get());
    }

    private WriteTaskUseCase writeTaskUseCase() {
      return new WriteTaskUseCase(singletonCImpl.webSocketZealotTransportProvider.get());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.commandViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.sessionListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.terminalViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.voiceViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(5).put(CommandViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) commandViewModelProvider)).put(SessionListViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) sessionListViewModelProvider)).put(SettingsViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) settingsViewModelProvider)).put(TerminalViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) terminalViewModelProvider)).put(VoiceViewModel_HiltModules_BindsModule_Binds_LazyMapKey.lazyClassKeyName, ((Provider) voiceViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // sh.delo.perth.feature.command.ui.CommandViewModel 
          return (T) new CommandViewModel(viewModelCImpl.interpretCommandUseCase(), viewModelCImpl.executeCommandUseCase(), singletonCImpl.settingsRepositoryImplProvider.get());

          case 1: // sh.delo.perth.feature.session.ui.SessionListViewModel 
          return (T) new SessionListViewModel(singletonCImpl.webSocketZealotTransportProvider.get(), singletonCImpl.sessionRepositoryImplProvider.get(), singletonCImpl.settingsRepositoryImplProvider.get(), viewModelCImpl.getSessionsUseCase(), viewModelCImpl.connectToSessionUseCase());

          case 2: // sh.delo.perth.feature.settings.ui.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.settingsRepositoryImplProvider.get(), singletonCImpl.sessionRepositoryImplProvider.get(), singletonCImpl.webSocketZealotTransportProvider.get(), singletonCImpl.llmRepositoryImplProvider.get());

          case 3: // sh.delo.perth.feature.terminal.ui.TerminalViewModel 
          return (T) new TerminalViewModel(singletonCImpl.webSocketZealotTransportProvider.get(), singletonCImpl.sessionRepositoryImplProvider.get(), singletonCImpl.settingsRepositoryImplProvider.get(), new NavigatePaneUseCase(), viewModelCImpl.sendInputUseCase());

          case 4: // sh.delo.perth.feature.voice.ui.VoiceViewModel 
          return (T) new VoiceViewModel(viewModelCImpl.captureVoiceUseCase(), singletonCImpl.voiceRepositoryImplProvider.get(), singletonCImpl.settingsRepositoryImplProvider.get(), singletonCImpl.webSocketZealotTransportProvider.get(), viewModelCImpl.writeTaskUseCase());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends PerthApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends PerthApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends PerthApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<SecureStorage> secureStorageProvider;

    private Provider<LlmRepositoryImpl> llmRepositoryImplProvider;

    private Provider<CommandSafetyGate> commandSafetyGateProvider;

    private Provider<WebSocketZealotTransport> webSocketZealotTransportProvider;

    private Provider<PerthDatabase> providePerthDatabaseProvider;

    private Provider<CommandAuditDao> provideCommandAuditDaoProvider;

    private Provider<CommandAuditRepositoryImpl> commandAuditRepositoryImplProvider;

    private Provider<UserPreferences> userPreferencesProvider;

    private Provider<SettingsRepositoryImpl> settingsRepositoryImplProvider;

    private Provider<SessionDao> provideSessionDaoProvider;

    private Provider<SessionRepositoryImpl> sessionRepositoryImplProvider;

    private Provider<AndroidSpeechRecognizer> androidSpeechRecognizerProvider;

    private Provider<SpeechRecognizer> bindPrimaryRecognizerProvider;

    private Provider<WhisperSpeechRecognizer> whisperSpeechRecognizerProvider;

    private Provider<SpeechRecognizer> bindFallbackRecognizerProvider;

    private Provider<VoiceRepositoryImpl> voiceRepositoryImplProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 1));
      this.secureStorageProvider = DoubleCheck.provider(new SwitchingProvider<SecureStorage>(singletonCImpl, 2));
      this.llmRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<LlmRepositoryImpl>(singletonCImpl, 0));
      this.commandSafetyGateProvider = DoubleCheck.provider(new SwitchingProvider<CommandSafetyGate>(singletonCImpl, 3));
      this.webSocketZealotTransportProvider = DoubleCheck.provider(new SwitchingProvider<WebSocketZealotTransport>(singletonCImpl, 4));
      this.providePerthDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<PerthDatabase>(singletonCImpl, 7));
      this.provideCommandAuditDaoProvider = DoubleCheck.provider(new SwitchingProvider<CommandAuditDao>(singletonCImpl, 6));
      this.commandAuditRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<CommandAuditRepositoryImpl>(singletonCImpl, 5));
      this.userPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<UserPreferences>(singletonCImpl, 9));
      this.settingsRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SettingsRepositoryImpl>(singletonCImpl, 8));
      this.provideSessionDaoProvider = DoubleCheck.provider(new SwitchingProvider<SessionDao>(singletonCImpl, 11));
      this.sessionRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<SessionRepositoryImpl>(singletonCImpl, 10));
      this.androidSpeechRecognizerProvider = new SwitchingProvider<>(singletonCImpl, 13);
      this.bindPrimaryRecognizerProvider = DoubleCheck.provider((Provider) androidSpeechRecognizerProvider);
      this.whisperSpeechRecognizerProvider = new SwitchingProvider<>(singletonCImpl, 14);
      this.bindFallbackRecognizerProvider = DoubleCheck.provider((Provider) whisperSpeechRecognizerProvider);
      this.voiceRepositoryImplProvider = DoubleCheck.provider(new SwitchingProvider<VoiceRepositoryImpl>(singletonCImpl, 12));
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    @Override
    public void injectPerthApp(PerthApp perthApp) {
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // sh.delo.perth.feature.command.data.LlmRepositoryImpl 
          return (T) new LlmRepositoryImpl(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.secureStorageProvider.get());

          case 1: // okhttp3.OkHttpClient 
          return (T) NetworkModule_Companion_ProvideOkHttpClientFactory.provideOkHttpClient();

          case 2: // sh.delo.perth.core.data.secure.SecureStorage 
          return (T) new SecureStorage(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // sh.delo.perth.feature.command.domain.CommandSafetyGate 
          return (T) new CommandSafetyGate();

          case 4: // sh.delo.perth.core.network.WebSocketZealotTransport 
          return (T) new WebSocketZealotTransport(singletonCImpl.provideOkHttpClientProvider.get());

          case 5: // sh.delo.perth.core.data.repository.CommandAuditRepositoryImpl 
          return (T) new CommandAuditRepositoryImpl(singletonCImpl.provideCommandAuditDaoProvider.get());

          case 6: // sh.delo.perth.core.data.db.dao.CommandAuditDao 
          return (T) DatabaseModule_ProvideCommandAuditDaoFactory.provideCommandAuditDao(singletonCImpl.providePerthDatabaseProvider.get());

          case 7: // sh.delo.perth.core.data.db.PerthDatabase 
          return (T) DatabaseModule_ProvidePerthDatabaseFactory.providePerthDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // sh.delo.perth.core.data.repository.SettingsRepositoryImpl 
          return (T) new SettingsRepositoryImpl(singletonCImpl.userPreferencesProvider.get(), singletonCImpl.secureStorageProvider.get());

          case 9: // sh.delo.perth.core.data.datastore.UserPreferences 
          return (T) new UserPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 10: // sh.delo.perth.core.data.repository.SessionRepositoryImpl 
          return (T) new SessionRepositoryImpl(singletonCImpl.webSocketZealotTransportProvider.get(), singletonCImpl.provideSessionDaoProvider.get());

          case 11: // sh.delo.perth.core.data.db.dao.SessionDao 
          return (T) DatabaseModule_ProvideSessionDaoFactory.provideSessionDao(singletonCImpl.providePerthDatabaseProvider.get());

          case 12: // sh.delo.perth.feature.voice.data.VoiceRepositoryImpl 
          return (T) new VoiceRepositoryImpl(singletonCImpl.bindPrimaryRecognizerProvider.get(), singletonCImpl.bindFallbackRecognizerProvider.get());

          case 13: // sh.delo.perth.feature.voice.data.AndroidSpeechRecognizer 
          return (T) new AndroidSpeechRecognizer(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 14: // sh.delo.perth.feature.voice.data.WhisperSpeechRecognizer 
          return (T) new WhisperSpeechRecognizer();

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
