<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

<!-- Bootstrap css -->
<link href="../../css/bootstrap.min.css" rel="stylesheet">

<!-- Housinggame player css -->
<link href="../../css/player.css" rel="stylesheet">

<!-- MUI symbols -->
<link href="../../iconfont/filled.css" rel="stylesheet">

<!-- Propeller css -->
<link href="../../css/propeller.min.css" rel="stylesheet">

<!-- jQuery before Propeller.js -->
<script type="text/javascript" src="../../js/jquery.min.js"></script>

<!-- Include all compiled plugins (below), or include individual files as needed -->
<script type="text/javascript" src="../../js/bootstrap.min.js"></script>
<script type="text/javascript" src="../../js/propeller.min.js"></script>

<!--  favicon -->
<link rel="shortcut icon" href="/housinggame-player/favicon.ico" type="image/x-icon">

<script>
initPage = function() {
    /* logged in? */
    var rn = String("${playerData.getPlayerCode()}");
    if (rn.length == 0 || rn == "null") {
      window.location = "/housinggame-player/login";
    }
}
</script>
