const WebUtil = () => {
}
const _block = function (el) {
	el.block({
		message: '<i class="fad fa-spinner fa-spin fa-3x text-primary"></i>',
		//timeout: 2000, //unblock after 2 seconds
		overlayCSS: {
			backgroundColor: '#fff',
			opacity: 0.8,
			cursor: 'wait'
		},
		css: {
			border: 0,
			padding: 0,
			backgroundColor: 'transparent'
		}
	});
}

const _unblock = function (el) {
	el.unblock();
}

WebUtil.block = function (el) {
	_block(el);
}

WebUtil.unblock = function (el) {
	_unblock(el);
}

WebUtil.showSuccessMessage = function (jquery, xhr, messageOption, unblockEl) {
	var option;
	if (!xhr) {
		option = messageOption;
	} else {
		option = {
			title: xhr.statusText,
			messageText: '<p class="mt-2" style="max-height: 200px; overflow-y: auto;">' + xhr.responseJSON.data.message + '</p>',
			icon: 'success'
		}
	}
	Swal.fire({
		title: option.title,
		html: option.messageText,
		icon: option.icon,
		buttonsStyling: false,
		confirmButtonText: "OK",
		customClass: {
			confirmButton: "btn btn-warning"
		}
	}).then(function () {
		if (unblockEl !== null) {
			_unblock(unblockEl);
		}
	})
}

WebUtil.showErrorMessage = function (jquery, xhr, messageOption, unblockEl, img404) {
	var option;
	if (!xhr) {
		option = messageOption;
	} else {
		if (xhr.status === 404) {
			if( typeof img404 !== 'undefined' && img404 !== null) {
				option = {
					title: "Not Found [404]",
					messageText: '<img class="img-fluid w-50 mt-2" src="'+ img404 + '" alt="">',
					icon: 'error'
				};
			} else {
				option = {
					title: "Not Found [404]",
					messageText: 'Path Not Found',
					icon: 'error'
				};
			}

		} else {
			option = {
				title: xhr.statusText,
				messageText: '<p class="mt-2" style="max-height: 200px; overflow-y: auto;">' + xhr.responseJSON.data.message + '</p>',
				icon: 'error'
			}
		}
	}
	Swal.fire({
		title: option.title,
		html: option.messageText,
		icon: option.icon,
		buttonsStyling: false,
		confirmButtonText: "OK",
		customClass: {
			confirmButton: "btn btn-danger"
		}
	}).then(function () {
		if (unblockEl !== null) {
			_unblock(unblockEl);
		}
	})
}
