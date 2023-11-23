<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="head.jsp"></jsp:include>
	<title>Housing Game: Select House</title>
	
<style>
.hg-house-row {
  display: flex;
  flex-direction: row;
  padding: 2px;
  margin: 2px;  
}

.hg-house-icon {
  padding: 2px;
  margin: 2px;
  display-vertical: center;
  opacity: 50%;
}
</style>

</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
		<jsp:include page="header.jsp"></jsp:include>
		
		<h1>HOUSE SELECTION</h1>
		
    <form action="/housinggame-player/house-calc" method="post">
      <div class="form-group pmd-textfield form-group-sm">
        <label for="gamesession" class="control-label pmd-textfield-floating-label">Select house</label> 
        <select name="houses" id="houses" class="form-control">
           ${playerData.getContentHtml("house/options") }
        </select>
      </div>
      
      <div>
        ${playerData.getContentHtml("house/details") }
        <!-- div class="house-details" id="house-details-D01" style="display: none;">
          <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">euro</i></div>
            <div class="hg-house-text">
              Price:425k<br>Yearly Mortgage (payment per round): 42.5k
            </div>
          </div>
          <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">star</i></div>
            <div class="hg-house-text">
              House Rate: 9<br>Your satisfaction will be affected by this
            </div>
          </div>
          <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">thunderstorm</i></div>
            <div class="hg-house-text">
              Pluvial protection: 0<br>Amount of protection from rain flooding
            </div>
          </div>
          <div class="hg-house-row">
            <div class="hg-house-icon"><i class="material-icons md-36">houseboat</i></div>
            <div class="hg-house-text">
              Fluvial protection: 0<br>Amount of protection from river flooding
            </div>
          </div>
        </div>
        <div class="house-details" id="house-details-N04" style="display: none;">
          <div>
            Price:200k<br>Yearly Mortgage (payment per round): 20k
          </div>
          <div>
            House Rate: 6<br>Your satisfaction will be affected by this
          </div>
          <div>
            Pluvial protection: 0<br>This is the amount of protection you have from rain flooding
          </div>
          <div>
            Fluvial protection: 0<br>This is the amount of protection you have from river flooding
          </div>
        </div -->
      </div>
		
      <div class="hg-button">
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
		
	</div>
	
	<script>
	  $(document).ready(function() {
      $(".house-details").hide();
      $("#house-details-" + $("#houses").find(":selected").text()).show();
      check();
	  });
	  $('#houses').on('change', function() {
		  $(".house-details").hide();
		  $("#house-details-" + this.value).show();
		  $.post("/housinggame-player/get-round-status",
        function(data, status) {
          if (data !== "INIT" && $("#houses").val() !== "NONE") {
            $("#hg-submit").removeAttr("disabled");
          } else {
        	  $("#hg-submit").attr("disabled", "");
          }
        });
	  });
	  function check() {
		  $.post("/housinggame-player/get-round-status",
				function(data, status) {
					if (data !== "INIT" && $("#houses").val() !== "NONE") {
						$("#hg-submit").removeAttr("disabled");
					} else {
            setTimeout(check, 5000);
		      }
				});
	  }
	</script>
	
</body>
</html>
