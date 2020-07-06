
define(['angular',
  // 'https://code.jquery.com/jquery-1.11.1.min.js',
  // 'https://code.jquery.com/mobile/1.5.0-rc1/jquery.mobile-1.5.0-rc1.min.js',
  // './jquery.js',
  // './jquery-mobile.js',
], function(angular) {

  var DashboardController = ["$scope", "$http", "Uri", function($scope, $http, Uri, $timeout) {
    $scope.test = "Hola";
    $scope.varName = "rrnr";

    $scope.check = function() {
      console.log("check");
      return false;
    };

    $scope.checkData = function(a) {
      console.log(a);
      return a;
    };


    $scope.submit = function() {
      $http({
        url: Uri.appUri("plugin://sample-plugin/:engine/search/searchVariable"),
        method: "GET",
        params: {varName: $scope.varName, varValue : $scope.varValue}
      }).then(function(res) {
        console.log('Got data');
        $scope.stateData = res.data;
      });
      //addCollapsibleListeners();
    };

    //addCollapsibleListeners();

    // loadCss("https://code.jquery.com/mobile/1.5.0-rc1/jquery.mobile-1.5.0-rc1.min.css");
    // function loadCss(url) {
    //   var link = document.createElement("link");
    //   link.type = "text/css";
    //   link.rel = "stylesheet";
    //   link.href = url;
    //   document.getElementsByTagName("head")[0].appendChild(link);
    // }

    function addCollapsibleListeners() {
      let i;
      let coll = document.getElementsByClassName("collapsible");
      for (i = 0; i < coll.length; i++) {
        coll[i].addEventListener("click", handleClick);
      }
    }

    $scope.handleClick = function ($event) {
      handleClick($event.target);
    };
    function handleClick(target) {
      target.classList.toggle("active");
      var content = target.nextElementSibling;
      if (content.style.display === "block") {
        content.style.display = "none";
      } else {
        content.style.display = "block";
      }
    }

  }];

  var Configuration = ['ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisions.dashboard', {
      id: 'process-definitions',
      label: 'Deployed Processes',
      url: 'plugin://sample-plugin/static/app/dashboard.html',
      dashboardMenuLabel: 'Sample',
      controller: DashboardController,

      // make sure we have a higher priority than the default plugin
      priority: 12
    });
  }];

  var ngModule = angular.module('cockpit.plugin.sample-plugin', []);

  ngModule.config(Configuration);

  return ngModule;
});
