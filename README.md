# WeblateFX
WeblateFX is a JavaFX-based Weblate client

:warning: Please note that WeblateFX works with translations as files, so only one 
translator should work on a file to avoid overwriting other's work. Currently, Weblate only supports locking translations at the component level and not at the file level.

Dependencies
------------
* JavaFX 11+
* jweblate (from https://github.com/javierllorente/jweblate)
* jgettext (from https://github.com/javierllorente/jgettext)
* ikonli-javafx
* ikonli-icomoon-pack
* java-diff-utils

Build & Run with Maven
------------
`mvn javafx:run`

Keyboard shortcuts
------------
| Task        | Shortcut    |
| ----------- | ----------- |
| Quick access to projects | Ctrl + 1 |
| Quick access to components | Ctrl + 2 | 
| Quick access to languages | Ctrl + 3 |
| Previous entry | Ctrl + , |
| Next entry | Ctrl + . |
| Quick search | Ctrl + F |
| Focus translation area | Ctrl + D |
| Switch tabs | Ctrl + T |
| Submit changes | Ctrl + S |
| Quit | Ctrl + Q |

Screenshot
------------
![Main window](screenshot.png)
