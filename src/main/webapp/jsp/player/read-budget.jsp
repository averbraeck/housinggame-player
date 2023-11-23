<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Budget Overview</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
    <jsp:include page="header.jsp"></jsp:include>

	  <h1 style="text-align: center; color: blue;">Budget overview</h1>
	  
	  <p>
	    Budget bla bla
	  </p>
	  
    <form action="/housinggame-player/check-status" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="read-budget" />
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
		
	</div>
	
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'read-budget'},
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
