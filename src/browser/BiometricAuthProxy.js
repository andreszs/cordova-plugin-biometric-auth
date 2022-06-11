var BiometricAuth = function () {};

BiometricAuth.prototype.AVAILABLE = "AVAILABLE";
BiometricAuth.prototype.AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
BiometricAuth.prototype.BIOMETRIC_SUCCESS = "BIOMETRIC_SUCCESS";
BiometricAuth.prototype.BIOMETRIC_DISMISSED = "BIOMETRIC_DISMISSED";

/* Browser features */
BiometricAuth.prototype.HIGHEST_POSSIBLE_Z_INDEX = 2147483647;
BiometricAuth.prototype.FINGERPRINT_BG_DIV = 'cordova_fingerprint_plugin_bg';
BiometricAuth.prototype.FINGERPRINT_FG_DIV = 'cordova_fingerprint_plugin_fg';

/* Browser buttons */
BiometricAuth.prototype.ICON_SUCCESS_SRC = 'fingerprint-success.png';
BiometricAuth.prototype.ICON_SUCCESS_ID = 'cordova_plugin_fingerprint_icon_success';
BiometricAuth.prototype.ICON_DISMISS_SRC = 'fingerprint-chevron-left.png';
BiometricAuth.prototype.ICON_DISMISS_ID = 'cordova_plugin_fingerprint_icon_dismiss';
BiometricAuth.prototype.ICON_FAILED_SRC = 'fingerprint-close.png';
BiometricAuth.prototype.ICON_FAILED_ID = 'cordova_plugin_fingerprint_icon_failed';
BiometricAuth.prototype.ICON_SIZE = '';

BiometricAuth.prototype.authenticate = function (successCallback, errorCallback, args) {
	/* translucid background div */
	var dialogBg = document.createElement('div');
	dialogBg.id = BiometricAuth.prototype.FINGERPRINT_BG_DIV;
	dialogBg.style.backgroundColor = '#00000088';
	dialogBg.style.position = 'fixed';
	dialogBg.style.top = '0';
	dialogBg.style.bottom = '0';
	dialogBg.style.left = '0';
	dialogBg.style.right = '0';
	dialogBg.style.zIndex = (BiometricAuth.prototype.HIGHEST_POSSIBLE_Z_INDEX - 1);
	document.body.appendChild(dialogBg);

	/* foreground text div */
	var dialogFg = document.createElement('div');
	dialogFg.id = BiometricAuth.prototype.FINGERPRINT_FG_DIV;
	dialogFg.setAttribute('style', 'transform: translateY(-50%);');
	dialogFg.style.backgroundColor = '#424242';
	dialogFg.style.boxShadow = 'rgba(0,0,0,0.5) 0px 4px 24px';
	dialogFg.style.borderRadius = '3px';
	dialogFg.style.marginLeft = 'auto';
	dialogFg.style.marginRight = 'auto';
	dialogFg.style.padding = '1.5em';
	dialogFg.style.position = 'relative';
	dialogFg.style.top = '50%';
	dialogFg.style.textAlign = 'center';
	dialogFg.style.width = '70%';
	dialogFg.style.zIndex = BiometricAuth.prototype.HIGHEST_POSSIBLE_Z_INDEX;

	/* title */
	var title = '';
	if (args && typeof (args[0]) === 'object' && typeof (args[0].title) === 'string' && args[0].title.length > 0) {
		title = args[0].title;
	} else {
		title = 'BiometricAuth Sign On';
	}
	var divTitle = document.createElement('div');
	divTitle.appendChild(document.createTextNode(title));
	divTitle.style.color = '#FFFFFF';
	divTitle.style.fontSize = '20px';
	divTitle.style.fontWeight = '600';
	divTitle.style.lineHeight = '2em';
	divTitle.style.textAlign = 'center';
	dialogFg.appendChild(divTitle);

	/* optional subtitle */
	if (args && typeof (args[0]) === 'object' && typeof (args[0].subtitle) === 'string' && args[0].subtitle.length > 0) {
		var divSubtitle = document.createElement('div');
		divSubtitle.appendChild(document.createTextNode(args[0].subtitle));
		divSubtitle.style.color = '#BDBDBD';
		divSubtitle.style.fontSize = '14px';
		divSubtitle.style.lineHeight = '2em';
		divSubtitle.style.textAlign = 'center';
		dialogFg.appendChild(divSubtitle);
	}

	/* optional description */
	if (args && typeof (args[0]) === 'object' && typeof (args[0].description) === 'string' && args[0].description.length > 0) {
		var divDescription = document.createElement('div');
		divDescription.appendChild(document.createTextNode(args[0].description));
		divDescription.style.color = '#BDBDBD';
		divDescription.style.fontSize = '14px';
		divDescription.style.lineHeight = '2em';
		divDescription.style.textAlign = 'center';
		dialogFg.appendChild(divDescription);
	}

	/* BIOMETRIC_DISMISSED */
	var imgIconDismissed = document.createElement('img');
	imgIconDismissed.id = BiometricAuth.prototype.ICON_DISMISS_ID;
	imgIconDismissed.src = BiometricAuth.prototype.ICON_DISMISS_SRC;
	imgIconDismissed.style.backgroundColor = '#FFFFFF10';
	imgIconDismissed.style.borderColor = '#FFFFFF30';
	imgIconDismissed.style.borderRadius = '50%';
	imgIconDismissed.style.borderStyle = 'dashed';
	imgIconDismissed.style.borderWidth = '2px';
	imgIconDismissed.style.cursor = 'pointer';
	imgIconDismissed.style.display = 'inline-block';
	imgIconDismissed.style.margin = '8px';
	imgIconDismissed.style.marginTop = '24px';
	imgIconDismissed.style.padding = '8px';
	imgIconDismissed.style.minWidth = '32px';
	imgIconDismissed.style.maxWidth = '96px';
	imgIconDismissed.style.width = '20%';
	imgIconDismissed.title = BiometricAuth.prototype.BIOMETRIC_DISMISSED;
	imgIconDismissed.addEventListener('mouseover', function () {
		imgIconDismissed.style.backgroundColor = '#FFFFFF20';
	});
	imgIconDismissed.addEventListener('mouseout', function () {
		imgIconDismissed.style.backgroundColor = '#FFFFFF10';
	});
	imgIconDismissed.addEventListener('click', function () {
		document.getElementById(BiometricAuth.prototype.FINGERPRINT_BG_DIV).remove();
		errorCallback(BiometricAuth.prototype.BIOMETRIC_DISMISSED);
	});
	dialogFg.appendChild(imgIconDismissed);

	/* BIOMETRIC_SUCCESS */
	var imgIconSuccess = document.createElement('img');
	imgIconSuccess.id = BiometricAuth.prototype.ICON_SUCCESS_ID;
	imgIconSuccess.src = BiometricAuth.prototype.ICON_SUCCESS_SRC;
	imgIconSuccess.style.backgroundColor = '#FFFFFF10';
	imgIconSuccess.style.borderColor = '#FFFFFF30';
	imgIconSuccess.style.borderRadius = '50%';
	imgIconSuccess.style.borderStyle = 'dashed';
	imgIconSuccess.style.borderWidth = '2px';
	imgIconSuccess.style.cursor = 'pointer';
	imgIconSuccess.style.display = 'inline-block';
	imgIconSuccess.style.margin = '8px';
	imgIconSuccess.style.marginTop = '24px';
	imgIconSuccess.style.padding = '8px';
	imgIconSuccess.style.minWidth = '32px';
	imgIconSuccess.style.maxWidth = '96px';
	imgIconSuccess.style.width = '20%';
	imgIconSuccess.title = BiometricAuth.prototype.STR_BIOMETRIC_SUCCESS;
	imgIconSuccess.addEventListener('mouseover', function () {
		imgIconSuccess.style.backgroundColor = '#FFFFFF20';
	});
	imgIconSuccess.addEventListener('mouseout', function () {
		imgIconSuccess.style.backgroundColor = '#FFFFFF10';
	});
	imgIconSuccess.addEventListener('click', function () {
		document.getElementById(BiometricAuth.prototype.FINGERPRINT_BG_DIV).remove();
		successCallback(BiometricAuth.prototype.BIOMETRIC_SUCCESS);
	});
	dialogFg.appendChild(imgIconSuccess);

	/* BIOMETRIC_AUTHENTICATION_FAILED */
	var imgIconFailed = document.createElement('img');
	imgIconFailed.id = BiometricAuth.prototype.ICON_FAILED_ID;
	imgIconFailed.src = BiometricAuth.prototype.ICON_FAILED_SRC;
	imgIconFailed.style.backgroundColor = '#FFFFFF10';
	imgIconFailed.style.borderColor = '#FFFFFF30';
	imgIconFailed.style.borderRadius = '50%';
	imgIconFailed.style.borderStyle = 'dashed';
	imgIconFailed.style.borderWidth = '2px';
	imgIconFailed.style.cursor = 'pointer';
	imgIconFailed.style.display = 'inline-block';
	imgIconFailed.style.margin = '8px';
	imgIconFailed.style.marginTop = '24px';
	imgIconFailed.style.padding = '8px';
	imgIconFailed.style.minWidth = '32px';
	imgIconFailed.style.maxWidth = '96px';
	imgIconFailed.style.width = '20%';
	imgIconFailed.title = BiometricAuth.prototype.AUTHENTICATION_FAILED;
	imgIconFailed.addEventListener('mouseover', function () {
		imgIconFailed.style.backgroundColor = '#FFFFFF20';
	});
	imgIconFailed.addEventListener('mouseout', function () {
		imgIconFailed.style.backgroundColor = '#FFFFFF10';
	});
	imgIconFailed.addEventListener('click', function () {
		document.getElementById(BiometricAuth.prototype.FINGERPRINT_BG_DIV).remove();
		errorCallback(BiometricAuth.prototype.AUTHENTICATION_FAILED);
	});
	dialogFg.appendChild(imgIconFailed);

	document.getElementById(BiometricAuth.prototype.FINGERPRINT_BG_DIV).appendChild(dialogFg);
};

BiometricAuth.prototype.isAvailable = function (successCallback, errorCallback, args) {
	successCallback(BiometricAuth.prototype.BIOMETRIC_SUCCESS);
};

module.exports = new BiometricAuth();

require('cordova/exec/proxy').add('BiometricAuth', module.exports);