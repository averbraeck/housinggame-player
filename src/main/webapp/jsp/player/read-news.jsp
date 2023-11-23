<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
	<jsp:include page="head.jsp"></jsp:include>
	<title>Housing Game News</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
		<jsp:include page="header.jsp"></jsp:include>
		
		<h1>News for the current round</h1>
		
		<b>
		${playerData.getContentHtml("news/summary/1") }
		</b>
		
		<p style="margin-top: 1em;">
		${playerData.getContentHtml("news/content/1") }
		</p>
		
   <form action="/housinggame-player/advance-state" method="post">
      <div class="hg-button">
        <input type="hidden" name="okButton" value="read-news" />
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
    
  </div>
  
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'read-news'},
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
