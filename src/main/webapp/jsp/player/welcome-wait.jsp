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
	  
    <form action="/housinggame-player/welcome" method="post">
      <table style="width:100%;">
        <tr>
          <td>
		        <span style="font-size: 200%; text-color:blue;"><b>${playerData.getLabel("welcome/wait/header") }</b></span><br/>
		        <b>${playerData.getLabel("welcome/wait/text") }</b>
          </td>
        </tr>
      </table>
      <div class="hg-button">
        <input type="submit" value='${playerData.getLabel("welcome/button/finish") }' class="btn btn-primary" />
      </div>
    </form>
		
	</div>
</body>
</html>
