<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Welcome</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
    <jsp:include page="header.jsp"></jsp:include>

	  <h1 style="text-align: center; color: blue;">${playerData.getLabel("welcome/header") }</h1>
	  
	  ${playerData.getLabel("welcome/text") }
	  
   <form action="/housinggame-player/advance-state" method="post">
      <div style="margin-top:15px; margin-bottom:15px;">
	      <table style="width:100%; border: 1px solid blue; border-collapse: collapse;">
	        <tr>
	          <td style="padding: 10px;">
			        <span style="font-size: 150%; text-color:blue;"><b>${playerData.getLabel("welcome/wait/header") }</b></span><br/>
			        <b>${playerData.getLabel("welcome/wait/text") }</b>
	          </td>
	        </tr>
	      </table>
      </div>
      <div class="hg-button">
        <input type="hidden" name="okButton" value="welcome-wait" />
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" id="hg-submit" disabled />
      </div>
    </form>
		
	</div>
	
  <script>
    $(document).ready(function() {
      check();
    });
    function check() {
      $.post("/housinggame-player/get-round-status", {jsp: 'welcome-wait'},
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
