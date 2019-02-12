# cordova-plugin-jj2000
Cordova plugin for JJ2000.

## Install
Install plugin:
```
cordova plugin add cordova-plugin-jj2000
```

## Usage
```
jj2000.convertJJ2000(rawImageBytes, function(photo) {
    var image = new Image();
    image.src = 'data:image/jpg;base64,' + photo;
    document.body.appendChild(image);
}, function(error) {
    alert('photo conversion failed');
});
```
