# Skin Server

This is a simple skin server to downloading, pre-caching and rendering

Current features:
- Render a skin head based on a file
- Render a skin/head based on a uuid or username
- Save skins into database for long-time cache
- Token based IP Selection for mojang requests


### Application properties 
```properties
skinserver.max-size=512 #Defines the maximum upscaling size of a skin/head
skinserver.min-size=16 #Defines the maximum downscaling size of a skin/head

skinserver.connection-addresses[0]=XXX.XXX.XXX.XX # Defines a array entry for used addresses
skinserver.connection-addresses[1]=XXX.XXX.XXX.XX # Defines a array entry for used addresses
```
