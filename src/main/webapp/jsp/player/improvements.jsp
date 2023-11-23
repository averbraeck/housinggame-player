<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Improvements</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
    <jsp:include page="header.jsp"></jsp:include>

	  <h1 style="text-align: center; color: blue;">Select and buy improvements</h1>
	  
	  <p>
	    Improvementsto the house and buy satisfaction
	  </p>
	  
   <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="improvements" />
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
		
	</div>
	
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'improvements'},
        function(data, status) {
          if (data == "OK") {
            $("#hg-submit").removeAttr("disabled");
          } else {
            setTimeout(check, 5000);
          }
        });
    }
  </script>
  
</body>
</html>
