local Library = require "CoronaLibrary"

local lib = Library:new{ name='plugin.huaweiAccountKit', publisherId='com.solar2d' }

local placeholder = function()
	print( "WARNING: The '" .. lib.name .. "' library is not available on this platform." )
end


lib.init = placeholder
lib.signIn = placeholder
lib.signOut = placeholder
lib.silentSignIn = placeholder
lib.cancelAuthorization = placeholder

-- Return an instance
return lib