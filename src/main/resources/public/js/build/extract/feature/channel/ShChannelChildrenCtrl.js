shioharaApp.controller('ShChannelChildrenCtrl', [
		"$scope",
		"$http",
		"$window",
		"$state",
		"$stateParams",
		"$rootScope",
		"Token",
		"shUserResource",
		"shChannelResource",
		"shPostResource",
		"shPostTypeResource",
		"shAPIServerService",
		'vigLocale',
		'$location',
		"$translate",
		"$filter",
		"Notification",
		function($scope, $http, $window, $state, $stateParams, $rootScope,
				Token, shUserResource, shChannelResource, shPostResource,
				shPostTypeResource, shAPIServerService, vigLocale, $location,
				$translate, $filter, Notification) {
			$scope.siteId = $stateParams.siteId;
			$scope.channelId = $stateParams.channelId;
			$scope.$parent.channelId = $stateParams.channelId;
			$scope.vigLanguage = vigLocale.getLocale().substring(0, 2);
			$translate.use($scope.vigLanguage);

			$scope.shSite = null;
			$scope.shChannels = null;
			$scope.shPosts = null;
			$scope.breadcrumb = null;
			$rootScope.$state = $state;
		
			$scope.$evalAsync($http.get(
					shAPIServerService.get().concat(
							"/channel/" + $scope.channelId + "/list")).then(
					function(response) {
						$scope.shChannels = response.data.shChannels;
						$scope.shPosts = response.data.shPosts;
						$scope.breadcrumb = response.data.breadcrumb;
						$scope.$parent.breadcrumb = response.data.breadcrumb;
						$scope.shSite = response.data.shSite;
						$scope.$parent.shSite = $scope.shSite;
					}));
			$scope.channelDelete = function(channelId) {
				$scope.shChannel = shChannelResource
				.get({
					id : channelId
				});
				shChannelResource
				.delete({
					id : channelId
				},function() {
					// filter the array
				    var foundItem = $filter('filter')($scope.shChannels, { id: channelId  }, true)[0];
				    // get the index
				    var index = $scope.shChannels.indexOf(foundItem );
				    // remove the item from array
				    $scope.shChannels.splice(index, 1);    		
					Notification.error('The '
							+ $scope.shChannel.name
							+ ' Channel was deleted.');
				});
			}
			
			$scope.postDelete = function(postId) {
				$scope.shPost = shPostResource
				.get({
					id : postId
				});
				shPostResource
				.delete({
					id : postId
				},function() {
					// filter the array
				    var foundItem = $filter('filter')($scope.shPosts, { id: postId  }, true)[0];
				    // get the index
				    var index = $scope.shPosts.indexOf(foundItem );
				    // remove the item from array
				    $scope.shPosts.splice(index, 1);    		
					Notification.error('The '
							+ $scope.shPost.title
							+ ' Post was deleted.');
				});
			}

		} ]);