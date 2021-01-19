# Huawei Account Kit Solar2d Plugin

This plugin was created based on Huawei Account Kit. Please [check](https://developer.huawei.com/consumer/en/hms/huawei-accountkit/) for detailed information about Huawei Account Kit. 

In order to use the Huawei Account kit, first you must create an account from developer.huawei.com. And after logging in with your account, and then you must create a project in the huawei console in order to use HMS kits.

## Project Setup

To use the plugin please add following to `build.settings`

```lua
{
    plugins = {
        ["plugin.huaweiAccountKit"] = {
            publisherId = "com.solar2d",
        },
    },
}
```

And then you have to create keystore for your app. And you must generate sha-256 bit fingerprint from this keystore using the command here. You have to define this fingerprint to your project on the huawei console.

And you must add the keystore you created while building your project. 
Also you need to give the package-name of the project you created on Huawei Console.
And you need to put `agconnect-services.json` file into `main.lua` directory.

After all the configuration processes, you must define the plugin in main.lua.

```lua
local accountKit = require "plugin.huaweiAccountKit"

local function listener(event)
    print(event) -- (table)
end

accountKit.init(listener) -- sets listener and inits HMS plugin
```

We should call all methods through accountKit object. And you can take result informations from listener.

## Methods in the Plugin

### signIn
Used for login operation. And you need pass 2 parameter. 
First one is authorization Parameter. 
* DEFAULT_AUTH_REQUEST_PARAM_GAME
* DEFAULT_AUTH_REQUEST_PARAM

Second one is information requests. Table type.
* setAuthorizationCode
* setAccessToken
* setEmail
* setId
* setIdToken
* setProfile

```lua
  accountKit.signIn("DEFAULT_AUTH_REQUEST_PARAM_GAME", {"setAuthorizationCode", "setAccessToken"})

    --Result 
    --[[Table {
              isError = true|false
              message = text
              type = signIn (text)
              provider = Huawei Account Kit
              data = {
                  getDisplayName,
                  getEmail,
                  getFamilyName,
                  getGivenName,
                  getAvatarUri,
                  ...
          }
        } 
    ]]--
```

### silentSignIn
silentSignIn method allows users to use the same HUAWEI ID without authorization for subsequent sign-ins.
And you should pass authorization parameter and information requests. Information requests are optional.. 
Authorization Parameter. 
* DEFAULT_AUTH_REQUEST_PARAM_GAME
* DEFAULT_AUTH_REQUEST_PARAM

Information Requests (Optional)
* setAuthorizationCode
* setAccessToken
* setEmail
* setId
* setIdToken
* setProfile

```lua
  accountKit.silentSignIn("DEFAULT_AUTH_REQUEST_PARAM_GAME")

    --Result 
    --[[Table {
              isError = true|false
              message = text
              type = silentSignIn (text)
              provider = Huawei Account Kit
              data = {
                  getDisplayName,
                  getEmail,
                  getFamilyName,
                  getGivenName,
                  getAvatarUri,
                  ...
          }
        } 
    ]]--
```

### signOut
The signOut method is called to sign out from a HUAWEI ID. 

```lua
    accountKit.signOut()

    --Result 
    --[[Table {
      isError = true|false
      message = text
      type = signOut (text)
      provider = Huawei Account Kit (text)
    }]]--
```


### cancelAuthorization
cancelAuthorization method is used to revoke authorization to improve privacy security on the app.
```lua
    accountKit.cancelAuthorization()

    --Result 
    --[[Table {
      isError = true|false
      message = text
      type = cancelAuthorization (text)
      provider = Huawei Account Kit (text) 
    }]]--
```


## Requirement
SDK Platform 19 or later

## References
HMS Account Kit https://developer.huawei.com/consumer/en/hms/huawei-accountkit/

## License
MIT

