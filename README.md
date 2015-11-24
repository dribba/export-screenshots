# export-screenshots
Export screenshots from chrome dev tools timeline

## Starting the app
* Clone repo
* `cd repo`
* `./activator run` to start server in port 9000 `./activator "run <port>"` if you want to run the app in a different port(I like to keep mine always running in port 9500 so it doesn't collide with my local play instance)

## How to use
* Pick a website  
![pick_website](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/Site.png "Website")
* Open DevTools in timeline tab
* Select screenshots(I'd recommend unchecking the rest if you don't want them, it will make the upload faster)  
![dev_tools](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/DevTools.png "DevTools")
* From the dev tools, use `⌘R` in OSX or `ctrl-R` to reload and record
* Right click on the screenshots and save the results  
![save_timeline](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/Saving%20timeline.png "Save Timeline")
(a `.json` extension wouldn't hurt)  
![save_json](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/Saving%20JSON.png "Save JSON")
* Upload and send  
![open_app](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/Open%20app.png "Open app")
* Get a zip with the images  
![get_zip](https://raw.githubusercontent.com/dribba/export-screenshots/master/img/Zip.png "Get zip")