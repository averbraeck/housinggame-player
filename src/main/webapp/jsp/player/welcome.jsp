<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->

<title>Housing Game Welcome</title>

<!-- Bootstrap css -->
<link href="../../css/bootstrap.min.css" rel="stylesheet">

<!-- MUI symbols -->
<link href="../../iconfont/filled.css" rel="stylesheet">

<!-- Propeller css -->
<link href="../../css/propeller.min.css" rel="stylesheet">

<!-- jQuery before Propeller.js -->
<script type="text/javascript" src="../../js/jquery.min.js"></script>

<!-- Include all compiled plugins (below), or include individual files as needed -->
<script type="text/javascript" src="../../js/bootstrap.min.js"></script>
<script type="text/javascript" src="../../js/propeller.min.js"></script>

<!-- script src="/housinggame-player/js/player.js"></script -->

<script>
initPage = function() {
	  /* logged in? */
	  var rn = String("${playerData.getPlayerCode()}");
	  if (rn.length == 0 || rn == "null") {
	    window.location = "/housinggame-player/login";
	  }
}
</script>

<style>
.form-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  max-width: 30em;
  min-width: 20em;
  margin:auto;
}

form {
  max-width: 30em;
  
}

h1 {
  padding-bottom: 10px;
}

.hg-button {
  text-align: center;
  margin: auto;
}

.hg-header {
  margin: 0 auto;
  position: -webkit-sticky; /* Safari */
  position: sticky;
  top: 10px;
}

.hg-header-row {
  display: flex;
  flex-direction: row;
  flex-wrap: wrap;
}

.hg-header-item {
  display: block;
  padding: 2px;
  margin: 2px;
  border: 1px solid blue;
  border-radius: 10px;
}

.hg-header-item > .material-icons {
  vertical-align: -6px;
 }
</style>

</head>

<body onLoad = initPage()>

	<div class="form-container">
		
		<p>Shaga</p>
	  <!--  header -->
	  <div class="hg-header">
	    <div class="hg-header-row">
	      <div class="hg-header-item" style="background-color: #ABDEEF;">
	        <span class="material-icons">group</span> 
	        ${playerData.getGroup().getName() }
	      </div>
        <div class="hg-header-item">
          <i class="material-icons">person</i>
          ${playerData.getPlayerCode() }
        </div>
        <div class="hg-header-item">
          <i class="material-icons">loop</i>
          ${playerData.getCurrentRound() }
        </div>
        <div class="hg-header-item">
          <i class="material-icons">stars</i>
          5
        </div>
        <div class="hg-header-item">
          40k
        </div>
	    </div>
	  </div>
	  
	  <h1 style="text-align: center;">${playerData.getLabel("welcome/header") }</h1>
	  
	  <!--  accordion -->
	  
	  <div class="panel-group pmd-accordion" id="welcome-accordion" role="tablist" aria-multiselectable="true" > 
		  
		  <div class="panel panel-default"> 
		    <div class="panel-heading" role="tab" id="heading1">
		      <h4 class="panel-title">
		        <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse1" aria-expanded="true" 
		          aria-controls="collapse1" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">1</span>
              ${playerData.getLabel("welcome/1/header") }
		          <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
		            keyboard_arrow_down
		          </i>
		        </a>
		      </h4>
		    </div>
		    <div id="collapse1" class="panel-collapse collapse in" role="tabpanel" aria-labelledby="heading1">
		      <div class="panel-body">
            ${playerData.getLabel("welcome/1/text") }
		      </div>
		    </div>
		  </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading2">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse2" aria-expanded="false" 
              aria-controls="collapse2" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">2</span>
              ${playerData.getLabel("welcome/2/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse2" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading2">
          <div class="panel-body">
            ${playerData.getLabel("welcome/2/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading3">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse3" aria-expanded="false" 
              aria-controls="collapse3" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">3</span>
              ${playerData.getLabel("welcome/3/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse3" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading3">
          <div class="panel-body">
            ${playerData.getLabel("welcome/3/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading4">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse4" aria-expanded="false" 
              aria-controls="collapse4" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">4</span>
              ${playerData.getLabel("welcome/4/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse4" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading4">
          <div class="panel-body">
            ${playerData.getLabel("welcome/4/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading5">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse5" aria-expanded="false" 
              aria-controls="collapse5" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">5</span>
              ${playerData.getLabel("welcome/5/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse5" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading5">
          <div class="panel-body">
            ${playerData.getLabel("welcome/5/text") }
          </div>
        </div>
      </div>

      <div class="panel panel-default"> 
        <div class="panel-heading" role="tab" id="heading6">
          <h4 class="panel-title">
            <a data-toggle="collapse" data-parent="#welcome-accordion" href="#collapse6" aria-expanded="false" 
              aria-controls="collapse6" data-expandable="false">
              <span class="badge badge-info pmd-ripple-effect">6</span>
              ${playerData.getLabel("welcome/6/header") }
              <i class="material-icons md-dark pmd-sm pmd-accordion-arrow">
                keyboard_arrow_down
              </i>
            </a>
          </h4>
        </div>
        <div id="collapse6" class="panel-collapse collapse" role="tabpanel" aria-labelledby="heading6">
          <div class="panel-body">
            ${playerData.getLabel("welcome/6/text") }
          </div>
        </div>
               
      </div>
		  
		</div>
		
    <form action="/housinggame-player/round" method="post">
        <div class="hg-button">
          <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" />
        </div>
      </form>
		

	</div>
</body>
</html>
