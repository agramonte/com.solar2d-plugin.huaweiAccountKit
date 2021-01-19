local accountKit = require "plugin.huaweiAccountKit"
local widget = require( "widget" )
local json = require("json")

local notLoggedIn = "Not logged in ..."
local userInfo

local header = display.newText( "Huawei Account Kit", display.contentCenterX, 60, native.systemFont, 10 )
header:setFillColor( 255, 255, 255 )

local displayName = display.newText( notLoggedIn, display.contentCenterX, 100, native.systemFont, 10 )
displayName:setFillColor( 255, 255, 255 )

local function listener( event )
    if event.type == "signIn" then
        if not event.isError then 
            userInfo = json.decode( event.data )
            displayName.text = "Welcome : " .. userInfo.getDisplayName
        else 
            displayName.text = "Error : " .. event.message
        end

    elseif event.type == "signOut" then
        if not event.isError then 
            displayName.text = notLoggedIn
        else 
            displayName.text = "Error : " .. event.message
        end

    elseif event.type == "silentSignIn" then
        if not event.isError then 
            userInfo = json.decode( event.data )
            displayName.text = "Welcome : " .. userInfo.getDisplayName
        else 
            displayName.text = "Error : " .. event.message
        end

    elseif event.type == "cancelAuthorization" then
        if not event.isError then 
            displayName.text = notLoggedIn
        else 
            displayName.text = "Error : " .. event.message
        end
        
    end
end

accountKit.init( listener )

-- Account Kit
local singIn = widget.newButton(
    {
        left = 65,
        top = 180,
        id = "singIn",
        label = "singIn",
        onPress = function()
            accountKit.signIn("DEFAULT_AUTH_REQUEST_PARAM_GAME", 
                {"setAuthorizationCode", "setAccessToken"})
        end,
        width = 190,
        height = 30
    }
)

local signOut = widget.newButton(
    {
        left = 65,
        top = 210,
        id = "signOut",
        label = "signOut",
        onPress = accountKit.signOut,
        width = 190,
        height = 30
    }
)


local cancelAuthorization = widget.newButton(
    {
        left = 65,
        top = 240,
        id = "cancelAuthorization",
        label = "cancelAuthorization",
        onPress = accountKit.cancelAuthorization,
        width = 190,
        height = 30
    }
)

local silentSignIn = widget.newButton(
    {
        left = 65,
        top = 270,
        id = "silentSignIn",
        label = "silentSignIn",
        onPress = function()
            accountKit.silentSignIn("DEFAULT_AUTH_REQUEST_PARAM_GAME", 
                {"setAccessToken"})
        end,
        width = 190,
        height = 30
    }
)



