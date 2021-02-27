![KATscans](github/logo.png)
#### A Java OpenGL volume renderer

KAT scans handles and load DAT, DCM, GRID, and RAW formats for volume rendering.

![Screen](github/screen.png)
###### It includes several volume raycasting/raymarching renderers. A few examples:
![Hand](github/hand.png)
![Skull](github/skull.png)
![Alpha](github/alpha.png)
![Maximum](github/maximum.png)
###### With easy to use interface. Like the transfer function editor:
![Histogram](github/histogram.png)

### Controls
|Input          |Modifier                 |Action                             |
|---------------|-------------------------|-----------------------------------|
|`1`            |`CTRL`                   |Open Datasets panel                |
|`Sapce`        |                         |Open selected dataset node actions |
|Mouse wheel    |                         |Zoom                               |
|Mouse          |`Middle button`          |Zoom                               |
|Mouse          |`Left button`            |Rotate                             |
|Mouse          |`Right button`           |Pan                                |
|Mouse wheel    |`ALT`                    |Field of view                      |
|Mouse          |`ALT`                    |Field of view                      |
|Mouse          |`CTRL` + `Left button`   |Upper cut-off threshold            |
|Mouse          |`CTRL` + `Right button`  |Lower cut-off threshold            |
|Mouse          |`CTRL` + `Middle button` |Upper and lower cut-off threshold  |
|Mouse          |`SHIFT`                  |Light position                     |
|Mouse wheel    |`SHIFT`                  |Slice through volume               |
|Mouse          |`SHIFT` + `Middle button`|Slice through volume               |
|Mouse          |`X` + `Left button`      |Upper slice cut on X axis          |
|Mouse          |`X` + `Right button`     |Lower slice cut on X axis          |
|Mouse          |`X` + `Right button`     |Upper and lower slice cut on X axis|
|Mouse          |`Y` + `Left button`      |Upper slice cut on Y axis          |
|Mouse          |`Y` + `Right button`     |Lower slice cut on Y axis          |
|Mouse          |`Y` + `Right button`     |Upper and lower slice cut on Y axis|
|Mouse          |`Z` + `Left button`      |Upper slice cut on Z axis          |
|Mouse          |`Z` + `Right button`     |Lower slice cut on Z axis          |
|Mouse          |`Z` + `Right button`     |Upper and lower slice cut on Z axis|
|Mouse          |`ALT` + `Left button`    |Stride length                      |
|Mouse          |Left to right            |Zoom histogram                     |
|Mouse          |Right to left            |Reset histogram zoom               |
|`Right button` |                         |Transfer function node color picker|
|`Middle button`|                         |Delete transfer function node      |
