<%@page import="nl.tudelft.simulation.housinggame.player.PlayerData"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <jsp:include page="head.jsp"></jsp:include>
  <title>Housing Game Error</title>
</head>

<body style="background-color: white;" onLoad = initPage()>

	<div class="form-container">
		
    <jsp:include page="header.jsp"></jsp:include>

	  <h1 style="text-align: center; color: blue;">Error</h1>
	  
    <form action="/housinggame-player/login" method="post">
      <div style="margin-top:15px; margin-bottom:15px;">
	      <table style="width:100%; border: 1px solid blue; border-collapse: collapse;">
	        <tr>
	          <td style="padding: 10px;">
			        <b>${playerData.getError() }</b>
	          </td>
	        </tr>
	      </table>
      </div>
      <div class="hg-button">
        <input type="submit" value='login' class="btn btn-primary" />
      </div>
    </form>
		
	</div>
</body>
</html>
