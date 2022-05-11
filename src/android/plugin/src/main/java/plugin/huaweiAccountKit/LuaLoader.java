//
//  LuaLoader.java
//  TemplateApp
//
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

// This corresponds to the name of the Lua library,
// e.g. [Lua] require "plugin.library"
package plugin.huaweiAccountKit;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ansca.corona.CoronaActivity;
import com.ansca.corona.CoronaEnvironment;
import com.ansca.corona.CoronaLua;
import com.ansca.corona.CoronaRuntime;
import com.ansca.corona.CoronaRuntimeListener;
import com.ansca.corona.CoronaRuntimeTask;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.agconnect.config.LazyInputStream;
import com.huawei.hmf.tasks.OnCompleteListener;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;
import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.LuaType;
import com.naef.jnlua.NamedJavaFunction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;


@SuppressWarnings("WeakerAccess")
public class LuaLoader implements JavaFunction, CoronaRuntimeListener {
    /**
     * Lua registry ID to the Lua function to be called when the ad request finishes.
     */
    private int fListener;

    public static final String TAG = "Huawei Account Kit";

    /**
     * This corresponds to the event name, e.g. [Lua] event.name
     */
    private static final String EVENT_NAME = "Huawei Account Kit";
    private static final String VERSION = "1.0.1";


    private static final String signIn = "signIn";
    private static final String signOut = "signOut";
    private static final String silentSignIn = "silentSignIn";
    private static final String cancelAuthorization = "cancelAuthorization";

    private static HuaweiIdAuthParams authParams;
    private static HuaweiIdAuthService service;

    /**
     * Creates a new Lua interface to this plugin.
     * <p>
     * Note that a new LuaLoader instance will not be created for every CoronaActivity instance.
     * That is, only one instance of this class will be created for the lifetime of the application process.
     * This gives a plugin the option to do operations in the background while the CoronaActivity is destroyed.
     */
    @SuppressWarnings("unused")
    public LuaLoader() {
        // Initialize member variables.
        fListener = CoronaLua.REFNIL;

        // Set up this plugin to listen for Corona runtime events to be received by methods
        // onLoaded(), onStarted(), onSuspended(), onResumed(), and onExiting().
        CoronaEnvironment.addRuntimeListener(this);
    }

    /**
     * Called when this plugin is being loaded via the Lua require() function.
     * <p>
     * Note that this method will be called every time a new CoronaActivity has been launched.
     * This means that you'll need to re-initialize this plugin here.
     * <p>
     * Warning! This method is not called on the main UI thread.
     *
     * @param L Reference to the Lua state that the require() function was called from.
     * @return Returns the number of values that the require() function will return.
     * <p>
     * Expected to return 1, the library that the require() function is loading.
     */
    @Override
    public int invoke(LuaState L) {
        // Register this plugin into Lua with the following functions.
        NamedJavaFunction[] luaFunctions = new NamedJavaFunction[]{
                new init(),
                new signIn(),
                new signOut(),
                new silentSignIn(),
                new cancelAuthorization()
        };
        String libName = L.toString(1);
        L.register(libName, luaFunctions);

        // Returning 1 indicates that the Lua require() function will return the above Lua library.
        return 1;
    }


    @Override
    public void onLoaded(CoronaRuntime runtime) {
    }

    @Override
    public void onStarted(CoronaRuntime runtime) {
        Log.i(EVENT_NAME, "Started v"+VERSION);
    }


    @Override
    public void onSuspended(CoronaRuntime runtime) {
    }


    @Override
    public void onResumed(CoronaRuntime runtime) {
    }

    @Override
    public void onExiting(CoronaRuntime runtime) {
        // Remove the Lua listener reference.
        CoronaLua.deleteRef(runtime.getLuaState(), fListener);
        fListener = CoronaLua.REFNIL;
    }

    /**
     * Simple example on how to dispatch events to Lua. Note that events are dispatched with
     * Runtime dispatcher. It ensures that Lua is accessed on it's thread to avoid race conditions
     *
     * @param message simple string to sent to Lua in 'message' field.
     */
    @SuppressWarnings("unused")
    public void dispatchEvent(final String message) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(message);
                L.setField(-2, "message");

                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }
            }
        });
    }

    @SuppressWarnings("unused")
    public void dispatchEvent(final Boolean isError, final String message, final String type, final String provider, final JSONObject data) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(message);
                L.setField(-2, "message");

                L.pushBoolean(isError);
                L.setField(-2, "isError");

                L.pushString(type);
                L.setField(-2, "type");

                L.pushString(provider);
                L.setField(-2, "provider");

                L.pushString(data.toString());
                L.setField(-2, "data");
                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }

            }
        });
    }

    @SuppressWarnings("unused")
    public void dispatchEvent(final Boolean isError, final String message, final String type, final String provider) {
        CoronaEnvironment.getCoronaActivity().getRuntimeTaskDispatcher().send(new CoronaRuntimeTask() {
            @Override
            public void executeUsing(CoronaRuntime runtime) {
                LuaState L = runtime.getLuaState();

                CoronaLua.newEvent(L, EVENT_NAME);

                L.pushString(message);
                L.setField(-2, "message");

                L.pushBoolean(isError);
                L.setField(-2, "isError");

                L.pushString(type);
                L.setField(-2, "type");

                L.pushString(provider);
                L.setField(-2, "provider");

                try {
                    CoronaLua.dispatchEvent(L, fListener, 0);
                } catch (Exception ignored) {
                }
            }
        });
    }

    /**
     * Implements the huaweiAccountKit.init() Lua function.
     */
    @SuppressWarnings("unused")
    private class init implements NamedJavaFunction {

        @Override
        public String getName() {
            return "init";
        }

        @Override
        public int invoke(LuaState L) {
            int listenerIndex = 1;

            if (CoronaLua.isListener(L, listenerIndex, EVENT_NAME)) {
                fListener = CoronaLua.newRef(L, listenerIndex);
            }

            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            AGConnectServicesConfig config = AGConnectServicesConfig.fromContext(activity);
            config.overlayWith(new LazyInputStream(activity) {
                public InputStream get(Context context) {
                    try {
                        Log.i(TAG, "agconnect-services.json ");
                        return context.getAssets().open("agconnect-services.json");
                    } catch (IOException e) {
                        Log.i(TAG, "agconnect-services.json reading Exception " + e);
                        return null;
                    }
                }
            });
            return 0;
        }
    }

    private JSONObject authHuaweiIdToJson(AuthHuaweiId authHuaweiId) {
        JSONObject _authHuaweiId = new JSONObject();
        CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
        try {
            _authHuaweiId.put("getAccessToken", authHuaweiId.getAccessToken());
            _authHuaweiId.put("getIdToken", authHuaweiId.getIdToken());
            _authHuaweiId.put("getDisplayName", authHuaweiId.getDisplayName());
            _authHuaweiId.put("getEmail", authHuaweiId.getEmail());
            _authHuaweiId.put("getFamilyName", authHuaweiId.getFamilyName());
            _authHuaweiId.put("getGivenName", authHuaweiId.getGivenName());
            _authHuaweiId.put("getAvatarUri", authHuaweiId.getAvatarUri());
            _authHuaweiId.put("getAuthorizationCode", authHuaweiId.getAuthorizationCode());
            _authHuaweiId.put("getUnionId", authHuaweiId.getUnionId());
            _authHuaweiId.put("getOpenId", authHuaweiId.getOpenId());
            _authHuaweiId.put("getHuaweiAccount", authHuaweiId.getHuaweiAccount(activity));
            _authHuaweiId.put("getAuthorizedScopes", authHuaweiId.getAuthorizedScopes());
            _authHuaweiId.put("getAgeRangeFlag", authHuaweiId.getAgeRangeFlag());
            _authHuaweiId.put("getAgeRange", authHuaweiId.getAgeRange());
            _authHuaweiId.put("getCountryCode", authHuaweiId.getCountryCode());
            _authHuaweiId.put("getGender", authHuaweiId.getGender());
            _authHuaweiId.put("getUid", authHuaweiId.getUid());
            _authHuaweiId.put("getHomeZone", authHuaweiId.getHomeZone());
            JSONArray AuthorizedScopes = new JSONArray();
            for (Scope scope : authHuaweiId.getAuthorizedScopes()) {
                JSONObject AuthorizedScope = new JSONObject();
                AuthorizedScope.put("getScopeUri", scope.getScopeUri());
                AuthorizedScope.put("describeContents", scope.describeContents());
                AuthorizedScope.put("hashCode", scope.hashCode());
                AuthorizedScope.put("toString", scope.toString());
                AuthorizedScopes.put(AuthorizedScope);
            }
            _authHuaweiId.put("getAuthorizedScopes", AuthorizedScopes);
        } catch (JSONException e) {
            Log.i(TAG, Objects.requireNonNull(e.getMessage()));
        }
        return _authHuaweiId;
    }

    @SuppressWarnings("unused")
    public class signIn implements NamedJavaFunction {

        @Override
        public String getName() {
            return signIn;
        }

        @Override
        public int invoke(final LuaState luaState) {
            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) {
                return 0;
            }

            HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper;

            if (luaState.type(1) == LuaType.STRING) {
                if ("DEFAULT_AUTH_REQUEST_PARAM_GAME".equals(luaState.toString(1))) {
                    huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME);
                } else {
                    huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
                }
            } else {
                dispatchEvent(true, "authorizationParameter(String) expected, got " + luaState.typeName(1), signIn, TAG);
                return 0;
            }


            if (luaState.type(2) == LuaType.TABLE) {
                // build supported ad types
                int ntypes = luaState.length(-1);
                if (ntypes > 0) {
                    for (int i = 1; i <= ntypes; i++) {
                        // push array value onto stack
                        luaState.rawGet(-1, i);
                        if (luaState.type(-1) != LuaType.STRING) {
                            continue;
                        }
                        switch (luaState.toString(-1)) {
                            case "setAuthorizationCode":
                                huaweiIdAuthParamsHelper.setAuthorizationCode();
                                break;
                            case "setAccessToken":
                                huaweiIdAuthParamsHelper.setAccessToken();
                                break;
                            case "setEmail":
                                huaweiIdAuthParamsHelper.setEmail();
                                break;
                            case "setId":
                                huaweiIdAuthParamsHelper.setId();
                                break;
                            case "setIdToken":
                                huaweiIdAuthParamsHelper.setIdToken();
                                break;
                            case "setProfile":
                                huaweiIdAuthParamsHelper.setProfile();
                                break;
                            default:
                                Log.i(TAG, "Unknown Obtain " + luaState.toString(-1));
                        }
                        luaState.pop(1);
                    }
                } else {
                    dispatchEvent(false, "Obtains table empty", signIn, TAG);
                }
            } else {
                dispatchEvent(true, "Obtains (table) expected, got" + luaState.typeName(-1), signIn, TAG);
                return 0;
            }

            int requestCode = activity.registerActivityResultHandler(new CoronaActivity.OnActivityResultHandler() {
                @Override
                public void onHandleActivityResult(CoronaActivity activity, int requestCode, int resultCode, Intent data) {
                    activity.unregisterActivityResultHandler(this);
                    Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
                    if (authHuaweiIdTask.isSuccessful()) {
                        AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                        dispatchEvent(false, "Huawei Account Kit SignIn Success", signIn, TAG, authHuaweiIdToJson(huaweiAccount));
                    } else {
                        dispatchEvent(true, "Huawei Account Kit SignIn Failed Status Code => " +
                                ((ApiException) authHuaweiIdTask.getException()).getStatusCode(), signIn, TAG);
                    }
                }
            });

            authParams = huaweiIdAuthParamsHelper.createParams();
            service = HuaweiIdAuthManager.getService(activity, authParams);
            activity.startActivityForResult(service.getSignInIntent(), requestCode);

            return 0;
        }
    }

    @SuppressWarnings("unused")
    public class signOut implements NamedJavaFunction {

        @Override
        public String getName() {
            return signOut;
        }

        @Override
        public int invoke(LuaState L) {
            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) {
                return 0;
            }

            if (service == null) {
                dispatchEvent(true, "Huawei Account Kit signOut failed because HuaweiIdAuthService is null", signOut, TAG);
                return 0;
            }

            Task<Void> signOutTask = service.signOut();

            signOutTask.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    dispatchEvent(false, "Huawei Account Kit signOut Success", signOut, TAG);
                }
            });

            signOutTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    dispatchEvent(true, "Huawei Account Kit signOut Failure Message "
                            + e.getMessage() + " / Cause " + e.getCause(), signOut, TAG);
                }
            });
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public class cancelAuthorization implements NamedJavaFunction {

        @Override
        public String getName() {
            return cancelAuthorization;
        }

        @Override
        public int invoke(LuaState L) {
            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) {
                return 0;
            }

            if (service == null) {
                dispatchEvent(true, "Huawei Account Kit cancelAuthorization failed because HuaweiIdAuthService is null", cancelAuthorization, TAG);
                return 0;
            }

            service.cancelAuthorization().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(Task<Void> task) {
                    if (task.isSuccessful()) {
                        dispatchEvent(false, "Huawei Account Kit cancelAuthorization Success", cancelAuthorization, TAG);
                    } else {
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            int statusCode = ((ApiException) exception).getStatusCode();
                            Log.i(TAG, "onFailure: " + statusCode);
                            dispatchEvent(true, "Huawei Account Kit cancelAuthorization Failure statusCode "
                                    + statusCode, cancelAuthorization, TAG);
                        }
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    dispatchEvent(true, "Huawei Account Kit cancelAuthorization Failure statusCode "
                            + e, cancelAuthorization, TAG);
                }
            });
            return 0;
        }
    }

    @SuppressWarnings("unused")
    public class silentSignIn implements NamedJavaFunction {

        @Override
        public String getName() {
            return silentSignIn;
        }

        @Override
        public int invoke(LuaState luaState) {
            CoronaActivity activity = CoronaEnvironment.getCoronaActivity();
            if (activity == null) {
                return 0;
            }

            HuaweiIdAuthParamsHelper huaweiIdAuthParamsHelper;

            if (luaState.type(1) == LuaType.STRING) {
                if ("DEFAULT_AUTH_REQUEST_PARAM_GAME".equals(luaState.toString(1))) {
                    huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM_GAME);
                } else {
                    huaweiIdAuthParamsHelper = new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
                }
            } else {
                dispatchEvent(true, "authorizationParameter(String) expected, got " + luaState.typeName(1), silentSignIn, TAG);
                return 0;
            }

            if (luaState.type(2) == LuaType.TABLE) {
                // build supported ad types
                int ntypes = luaState.length(-1);
                if (ntypes > 0) {
                    for (int i = 1; i <= ntypes; i++) {
                        // push array value onto stack
                        luaState.rawGet(-1, i);
                        if (luaState.type(-1) != LuaType.STRING) {
                            continue;
                        }
                        switch (luaState.toString(-1)) {
                            case "setAuthorizationCode":
                                huaweiIdAuthParamsHelper.setAuthorizationCode();
                                break;
                            case "setAccessToken":
                                huaweiIdAuthParamsHelper.setAccessToken();
                                break;
                            case "setEmail":
                                huaweiIdAuthParamsHelper.setEmail();
                                break;
                            case "setId":
                                huaweiIdAuthParamsHelper.setId();
                                break;
                            case "setIdToken":
                                huaweiIdAuthParamsHelper.setIdToken();
                                break;
                            case "setProfile":
                                huaweiIdAuthParamsHelper.setProfile();
                                break;
                            default:
                                Log.i(TAG, "Unknown Obtain " + luaState.toString(-1));
                        }
                        luaState.pop(1);
                    }
                }
            }

            authParams = huaweiIdAuthParamsHelper.createParams();
            service = HuaweiIdAuthManager.getService(activity, authParams);

            Task<AuthHuaweiId> task = service.silentSignIn();
            task.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
                @Override
                public void onSuccess(AuthHuaweiId authHuaweiId) {
                    dispatchEvent(false, "Huawei Account Kit silentSignIn Success", silentSignIn, TAG, authHuaweiIdToJson(authHuaweiId));
                }
            });
            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        Log.i(TAG, "sign failed status:" + apiException.getStatusCode());

                        dispatchEvent(true, "Huawei Account Kit silentSignIn Failed Status Code => " +
                                apiException.getStatusCode(), signIn, TAG);
                    }
                }
            });

            return 0;
        }
    }

}
