shioharaApp.controller('ShWidgetFileCtrl', [ '$scope', 'Upload', '$timeout',
		function($scope, Upload, $timeout) {
			$scope.fileName = null;
			$scope.uploadNewFile = false;
			$scope.$watch('shPostAttr.file', function() {
				if ($scope.shPostAttr.file != null) {
					$scope.uploadNewFile = false;
					$scope.fileName = $scope.shPostAttr.file.name;
					$scope.shPostAttr.strValue = $scope.fileName;
				}
			});

			$scope.newFile = function() {
				$scope.uploadNewFile = true;
			}
			$scope.clearFile = function(shPostAttr) {
				shPostAttr.strValue = null;
				shPostAttr.file = null;
			}
		} ]);